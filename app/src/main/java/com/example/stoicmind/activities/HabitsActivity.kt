package com.example.stoicmind.activities

import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stoicmind.adapters.HabitsAdapter
import com.example.stoicmind.database.FirebaseManager
import com.example.stoicmind.databinding.ActivityHabitsBinding
import com.example.stoicmind.models.Habit
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@AndroidEntryPoint
class HabitsActivity : BaseActivity() {

    @Inject
    lateinit var firebaseManager: FirebaseManager

    private lateinit var binding: ActivityHabitsBinding
    private lateinit var habitsAdapter: HabitsAdapter
    private var habitsList = mutableListOf<Habit>()

    private val db = FirebaseFirestore.getInstance()
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHabitsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        loadHabits()

        binding.btnAddNewHabit.setOnClickListener {
            showAddHabitDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        loadHabits()
    }

    private fun getUserId(): String {
        return firebaseManager.getCurrentUserId() ?: "guest_user"
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarInclude.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Habits"
        binding.toolbarInclude.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        habitsAdapter = HabitsAdapter(habitsList) { habit, isChecked ->
            onHabitChecked(habit, isChecked)
        }
        binding.rvHabits.apply {
            layoutManager = LinearLayoutManager(this@HabitsActivity)
            adapter = habitsAdapter
        }
    }

    private fun loadHabits() {
        val userId = getUserId()
        val prefs = getSharedPreferences("stoic_mind_prefs", Context.MODE_PRIVATE)
        val habitsJson = prefs.getString("stoic_mind_habits_$userId", null)

        if (habitsJson.isNullOrBlank()) {
            habitsList = getDefaultHabits(userId).toMutableList()
            saveHabitsLocally(userId, habitsList)
            saveHabitsToFirestore(userId, habitsList)
        } else {
            try {
                val listType = object : TypeToken<List<Habit>>() {}.type
                val savedList: List<Habit> = gson.fromJson(habitsJson, listType)
                habitsList = savedList.toMutableList()
            } catch (_: Exception) {
                habitsList = getDefaultHabits(userId).toMutableList()
            }
        }

        habitsAdapter.updateHabits(habitsList)
        syncHabitsFromFirestore(userId)
    }

    private fun getDefaultHabits(userId: String): List<Habit> {
        return listOf(
            Habit(id = "habit_1", userId = userId, name = "Read a book", colorHex = "#D1E8E2", isCompleted = false),
            Habit(id = "habit_2", userId = userId, name = "Meditate", colorHex = "#E0D7FF", isCompleted = false),
            Habit(id = "habit_3", userId = userId, name = "Exercise", colorHex = "#FFF0C1", isCompleted = false),
            Habit(id = "habit_4", userId = userId, name = "Journal", colorHex = "#C1F0D1", isCompleted = false),
            Habit(id = "habit_5", userId = userId, name = "Study", colorHex = "#FFC1C1", isCompleted = false)
        )
    }

    private fun onHabitChecked(habit: Habit, isChecked: Boolean) {
        val index = habitsList.indexOfFirst { it.id == habit.id || it.name == habit.name }
        if (index != -1) {
            val updated = habitsList[index].copy(isCompleted = isChecked)
            habitsList[index] = updated
            val userId = getUserId()
            saveHabitsLocally(userId, habitsList)
            saveHabitsToFirestore(userId, habitsList)

            val status = if (isChecked) "completed!" else "uncompleted."
            Toast.makeText(this, "${updated.name} marked as $status", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveHabitsLocally(userId: String, habits: List<Habit>) {
        val prefs = getSharedPreferences("stoic_mind_prefs", Context.MODE_PRIVATE)
        val json = gson.toJson(habits)
        prefs.edit().putString("stoic_mind_habits_$userId", json).apply()
    }

    private fun saveHabitsToFirestore(userId: String, habits: List<Habit>) {
        if (userId == "guest_user") return
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val data = mapOf("habitsList" to habits)
                db.collection("users").document(userId).set(data, SetOptions.merge()).await()
            } catch (_: Exception) {
                // Silently fallback to local SharedPreferences
            }
        }
    }

    private fun syncHabitsFromFirestore(userId: String) {
        if (userId == "guest_user") return
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val doc = db.collection("users").document(userId).get().await()
                if (doc.exists() && doc.contains("habitsList")) {
                    val rawList = doc.get("habitsList") as? List<*>
                    if (rawList != null) {
                        val remoteHabits = rawList.mapNotNull { item ->
                            if (item is Map<*, *>) {
                                val jsonStr = gson.toJson(item)
                                val jsonObject = com.google.gson.JsonParser.parseString(jsonStr).asJsonObject
                                if (jsonObject.has("completed") && !jsonObject.has("isCompleted")) {
                                    jsonObject.add("isCompleted", jsonObject.get("completed"))
                                }
                                if (jsonObject.has("active") && !jsonObject.has("isActive")) {
                                    jsonObject.add("isActive", jsonObject.get("active"))
                                }
                                gson.fromJson(jsonObject, Habit::class.java)
                            } else null
                        }
                        if (remoteHabits.isNotEmpty()) {
                            saveHabitsLocally(userId, remoteHabits)
                            launch(Dispatchers.Main) {
                                habitsList = remoteHabits.toMutableList()
                                habitsAdapter.updateHabits(habitsList)
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                // Ignore sync failures, local storage used
            }
        }
    }

    private fun showAddHabitDialog() {
        val input = EditText(this)
        input.hint = "Enter habit name"

        AlertDialog.Builder(this)
            .setTitle("Add New Habit")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val newHabitName = input.text.toString().trim()
                if (newHabitName.isNotEmpty()) {
                    val userId = getUserId()
                    val palette = listOf("#D1E8E2", "#E0D7FF", "#FFF0C1", "#C1F0D1", "#FFC1C1", "#FFE0B2", "#B2EBF2")
                    val colorHex = palette[habitsList.size % palette.size]
                    val newHabit = Habit(
                        id = "habit_${System.currentTimeMillis()}",
                        userId = userId,
                        name = newHabitName,
                        colorHex = colorHex,
                        isCompleted = false
                    )

                    habitsList.add(newHabit)
                    habitsAdapter.updateHabits(habitsList)
                    saveHabitsLocally(userId, habitsList)
                    saveHabitsToFirestore(userId, habitsList)

                    Toast.makeText(this, "Habit '$newHabitName' added successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Habit name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
