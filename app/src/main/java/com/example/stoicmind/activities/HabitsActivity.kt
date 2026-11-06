package com.example.stoicmind.activities

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.stoicmind.databinding.ActivityHabitsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HabitsActivity : BaseActivity() {

    private lateinit var binding: ActivityHabitsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHabitsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupHabits()
        
        binding.btnAddNewHabit.setOnClickListener {
            showAddHabitDialog()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarInclude.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Habits"
        binding.toolbarInclude.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupHabits() {
        val prefs = getSharedPreferences("stoic_habits_prefs", MODE_PRIVATE)

        val habit5Name = prefs.getString("custom_habit_5_name", "Study") ?: "Study"
        binding.habit5.tvHabitName.text = habit5Name

        val habits = listOf(
            binding.habit1 to ("Read a book" to "#D1E8E2"),
            binding.habit2 to ("Meditate" to "#E0D7FF"),
            binding.habit3 to ("Exercise" to "#FFF0C1"),
            binding.habit4 to ("Journal" to "#C1F0D1"),
            binding.habit5 to (habit5Name to "#FFC1C1")
        )

        habits.forEachIndexed { index, (itemBinding, data) ->
            itemBinding.tvHabitName.text = data.first
            itemBinding.cvIconBackground.setCardBackgroundColor(Color.parseColor(data.second))
            
            val isChecked = prefs.getBoolean("habit_checked_$index", index < 2)
            itemBinding.cbHabit.isChecked = isChecked

            itemBinding.cbHabit.setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("habit_checked_$index", checked).apply()
                val status = if (checked) "completed!" else "uncompleted."
                Toast.makeText(this, "${data.first} marked as $status", Toast.LENGTH_SHORT).show()
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
                    val prefs = getSharedPreferences("stoic_habits_prefs", MODE_PRIVATE)
                    prefs.edit().putString("custom_habit_5_name", newHabitName).apply()
                    binding.habit5.tvHabitName.text = newHabitName
                    binding.habit5.cbHabit.isChecked = false
                    Toast.makeText(this, "Habit '$newHabitName' added successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Habit name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
