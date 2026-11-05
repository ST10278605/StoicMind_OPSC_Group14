package com.example.stoicmind.activities

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.stoicmind.database.FirebaseManager
import com.example.stoicmind.databinding.ActivityRemindersBinding
import com.example.stoicmind.models.Quote
import com.example.stoicmind.notifications.NotificationHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class RemindersActivity : BaseActivity() {

    @Inject
    lateinit var firebaseManager: FirebaseManager

    private lateinit var binding: ActivityRemindersBinding
    private val db = FirebaseFirestore.getInstance()

    private var currentQuoteIndex = 0
    private val stoicQuotes = listOf(
        Quote(
            text = "You have power over your mind - not outside events. Realize this, and you will find strength.",
            author = "Marcus Aurelius"
        ),
        Quote(
            text = "We suffer more often in imagination than in reality.",
            author = "Seneca"
        ),
        Quote(
            text = "It's not what happens to you, but how you react to it that matters.",
            author = "Epictetus"
        ),
        Quote(
            text = "The obstacle is the way.",
            author = "Marcus Aurelius"
        ),
        Quote(
            text = "He who fears death will never do anything worthy of a man who is alive.",
            author = "Seneca"
        ),
        Quote(
            text = "Waste no more time arguing about what a good man should be. Be one.",
            author = "Marcus Aurelius"
        ),
        Quote(
            text = "If it is not right do not do it; if it is not true do not say it.",
            author = "Marcus Aurelius"
        ),
        Quote(
            text = "Luck is what happens when preparation meets opportunity.",
            author = "Seneca"
        ),
        Quote(
            text = "Wealth consists not in having great possessions, but in having few wants.",
            author = "Epictetus"
        ),
        Quote(
            text = "Wellbeing is attained little by little, and nevertheless is no little thing itself.",
            author = "Zeno of Citium"
        ),
        Quote(
            text = "No man is free who is not master of himself.",
            author = "Epictetus"
        ),
        Quote(
            text = "When you arise in the morning think of what a privilege it is to be alive, to think, to enjoy, to love.",
            author = "Marcus Aurelius"
        ),
        Quote(
            text = "If a man knows not which port he sails on, no wind is favorable.",
            author = "Seneca"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRemindersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupClickListeners()
        loadSavedSettings()
        setupListeners()
    }

    private fun getUserId(): String {
        return firebaseManager.getCurrentUserId() ?: "guest_user"
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarInclude.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Reminders"
        binding.toolbarInclude.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        binding.btnTestRealtimeAlert.setOnClickListener {
            val quote = stoicQuotes[currentQuoteIndex]
            currentQuoteIndex = (currentQuoteIndex + 1) % stoicQuotes.size

            NotificationHelper.showNotification(
                context = this,
                title = "Stoic Mind Real-Time Alert",
                body = "\"${quote.text}\" - ${quote.author}"
            )
            Toast.makeText(this, "Notification sent: ${quote.author}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadSavedSettings() {
        val userId = getUserId()
        val prefs = getSharedPreferences("stoic_mind_prefs", Context.MODE_PRIVATE)

        val morningEnabled = prefs.getBoolean("reminder_${userId}_morning_enabled", false)
        val morningTime = prefs.getString("reminder_${userId}_morning_time", "08:00 AM") ?: "08:00 AM"

        val eveningEnabled = prefs.getBoolean("reminder_${userId}_evening_enabled", false)
        val eveningTime = prefs.getString("reminder_${userId}_evening_time", "09:00 PM") ?: "09:00 PM"

        updateMorningUi(morningEnabled, morningTime)
        updateEveningUi(eveningEnabled, eveningTime)

        syncSettingsFromFirestore(userId)
    }

    private fun updateMorningUi(enabled: Boolean, time: String) {
        binding.switchMorning.isChecked = enabled
        binding.tvMorningTime.text = "Morning Reflection\n$time"
        binding.tvMorningTime.alpha = if (enabled) 1.0f else 0.6f
    }

    private fun updateEveningUi(enabled: Boolean, time: String) {
        binding.switchEvening.isChecked = enabled
        binding.tvEveningTime.text = "Evening Review\n$time"
        binding.tvEveningTime.alpha = if (enabled) 1.0f else 0.6f
    }

    private fun syncSettingsFromFirestore(userId: String) {
        if (userId == "guest_user") return
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val doc = db.collection("users").document(userId).get().await()
                if (doc.exists()) {
                    val firestoreMorningEnabled = doc.getBoolean("reminderMorningEnabled")
                    val firestoreMorningTime = doc.getString("reminderMorningTime")
                    val firestoreEveningEnabled = doc.getBoolean("reminderEveningEnabled")
                    val firestoreEveningTime = doc.getString("reminderEveningTime")

                    val prefs = getSharedPreferences("stoic_mind_prefs", Context.MODE_PRIVATE)
                    val editor = prefs.edit()
                    var hasUpdates = false

                    if (firestoreMorningEnabled != null) {
                        editor.putBoolean("reminder_${userId}_morning_enabled", firestoreMorningEnabled)
                        hasUpdates = true
                    }
                    if (firestoreMorningTime != null) {
                        editor.putString("reminder_${userId}_morning_time", firestoreMorningTime)
                        hasUpdates = true
                    }
                    if (firestoreEveningEnabled != null) {
                        editor.putBoolean("reminder_${userId}_evening_enabled", firestoreEveningEnabled)
                        hasUpdates = true
                    }
                    if (firestoreEveningTime != null) {
                        editor.putString("reminder_${userId}_evening_time", firestoreEveningTime)
                        hasUpdates = true
                    }

                    if (hasUpdates) {
                        editor.apply()
                        val mEnabled = prefs.getBoolean("reminder_${userId}_morning_enabled", false)
                        val mTime = prefs.getString("reminder_${userId}_morning_time", "08:00 AM") ?: "08:00 AM"
                        val eEnabled = prefs.getBoolean("reminder_${userId}_evening_enabled", false)
                        val eTime = prefs.getString("reminder_${userId}_evening_time", "09:00 PM") ?: "09:00 PM"
                        launch(Dispatchers.Main) {
                            updateMorningUi(mEnabled, mTime)
                            updateEveningUi(eEnabled, eTime)
                        }
                    }
                }
            } catch (_: Exception) {
                // Silently fallback to local SharedPreferences
            }
        }
    }

    private fun saveSetting(keySuffix: String, value: Any) {
        val userId = getUserId()
        val prefs = getSharedPreferences("stoic_mind_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        when (value) {
            is Boolean -> editor.putBoolean("reminder_${userId}_$keySuffix", value)
            is String -> editor.putString("reminder_${userId}_$keySuffix", value)
        }
        editor.apply()

        if (userId != "guest_user") {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val fieldName = when (keySuffix) {
                        "morning_enabled" -> "reminderMorningEnabled"
                        "morning_time" -> "reminderMorningTime"
                        "evening_enabled" -> "reminderEveningEnabled"
                        "evening_time" -> "reminderEveningTime"
                        else -> null
                    }
                    if (fieldName != null) {
                        val data = mapOf(fieldName to value)
                        db.collection("users").document(userId).set(data, SetOptions.merge()).await()
                    }
                } catch (_: Exception) {
                    // Local preferences persist if network sync is unavailable
                }
            }
        }
    }

    private fun setupListeners() {
        binding.switchMorning.setOnCheckedChangeListener { _, isChecked ->
            saveSetting("morning_enabled", isChecked)
            val prefs = getSharedPreferences("stoic_mind_prefs", Context.MODE_PRIVATE)
            val time = prefs.getString("reminder_${getUserId()}_morning_time", "08:00 AM") ?: "08:00 AM"
            updateMorningUi(isChecked, time)

            if (isChecked) {
                Toast.makeText(this, "Morning reminder enabled. Set your time.", Toast.LENGTH_SHORT).show()
                showTimePicker(isMorning = true)
            } else {
                Toast.makeText(this, "Morning reminder disabled", Toast.LENGTH_SHORT).show()
            }
        }

        binding.switchEvening.setOnCheckedChangeListener { _, isChecked ->
            saveSetting("evening_enabled", isChecked)
            val prefs = getSharedPreferences("stoic_mind_prefs", Context.MODE_PRIVATE)
            val time = prefs.getString("reminder_${getUserId()}_evening_time", "09:00 PM") ?: "09:00 PM"
            updateEveningUi(isChecked, time)

            if (isChecked) {
                Toast.makeText(this, "Evening reminder enabled. Set your time.", Toast.LENGTH_SHORT).show()
                showTimePicker(isMorning = false)
            } else {
                Toast.makeText(this, "Evening reminder disabled", Toast.LENGTH_SHORT).show()
            }
        }

        binding.cvMorning.setOnClickListener {
            if (!binding.switchMorning.isChecked) {
                binding.switchMorning.isChecked = true
            } else {
                showTimePicker(isMorning = true)
            }
        }

        binding.cvEvening.setOnClickListener {
            if (!binding.switchEvening.isChecked) {
                binding.switchEvening.isChecked = true
            } else {
                showTimePicker(isMorning = false)
            }
        }
    }

    private fun showTimePicker(isMorning: Boolean) {
        val prefs = getSharedPreferences("stoic_mind_prefs", Context.MODE_PRIVATE)
        val userId = getUserId()
        val currentTimeStr = if (isMorning) {
            prefs.getString("reminder_${userId}_morning_time", "08:00 AM") ?: "08:00 AM"
        } else {
            prefs.getString("reminder_${userId}_evening_time", "09:00 PM") ?: "09:00 PM"
        }

        val calendar = Calendar.getInstance()
        try {
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val date = timeFormat.parse(currentTimeStr)
            if (date != null) {
                val tempCal = Calendar.getInstance().apply { time = date }
                calendar.set(Calendar.HOUR_OF_DAY, tempCal.get(Calendar.HOUR_OF_DAY))
                calendar.set(Calendar.MINUTE, tempCal.get(Calendar.MINUTE))
            }
        } catch (_: Exception) {
        }

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val calendarSelected = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, selectedHour)
                    set(Calendar.MINUTE, selectedMinute)
                }
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val formattedTime = timeFormat.format(calendarSelected.time)

                if (isMorning) {
                    saveSetting("morning_time", formattedTime)
                    updateMorningUi(binding.switchMorning.isChecked, formattedTime)
                } else {
                    saveSetting("evening_time", formattedTime)
                    updateEveningUi(binding.switchEvening.isChecked, formattedTime)
                }
                Toast.makeText(this, "Reminder time set to $formattedTime", Toast.LENGTH_SHORT).show()
            },
            hour,
            minute,
            false
        )
        timePickerDialog.show()
    }
}
