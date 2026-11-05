package com.example.stoicmind.activities

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import com.example.stoicmind.databinding.ActivityRemindersBinding
import com.example.stoicmind.notifications.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class RemindersActivity : BaseActivity() {

    private lateinit var binding: ActivityRemindersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRemindersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupClickListeners()
        loadSavedSettings()
        setupListeners()
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
            NotificationHelper.showNotification(
                context = this,
                title = "Stoic Mind Real-Time Alert",
                body = "\"You have power over your mind - not outside events. Realize this, and you will find strength.\" - Marcus Aurelius"
            )
            Toast.makeText(this, "Real-Time notification dispatched!", Toast.LENGTH_SHORT).show()
        }
    }

}
    private fun loadSavedSettings() {
        val prefs = getSharedPreferences("stoic_mind_prefs", Context.MODE_PRIVATE)

        val morningEnabled = prefs.getBoolean("reminder_morning_enabled", true)
        val morningTime = prefs.getString("reminder_morning_time", "08:00 AM") ?: "08:00 AM"

        val eveningEnabled = prefs.getBoolean("reminder_evening_enabled", true)
        val eveningTime = prefs.getString("reminder_evening_time", "09:00 PM") ?: "09:00 PM"

        binding.switchMorning.isChecked = morningEnabled
        binding.tvMorningTime.text = "Morning Reflection\n$morningTime"

        binding.switchEvening.isChecked = eveningEnabled
        binding.tvEveningTime.text = "Evening Review\n$eveningTime"
    }

    private fun setupListeners() {
        val prefs = getSharedPreferences("stoic_mind_prefs", Context.MODE_PRIVATE)

        binding.switchMorning.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("reminder_morning_enabled", isChecked).apply()
            val stateText = if (isChecked) "enabled" else "disabled"
            Toast.makeText(this, "Morning reminder $stateText", Toast.LENGTH_SHORT).show()
        }

        binding.switchEvening.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("reminder_evening_enabled", isChecked).apply()
            val stateText = if (isChecked) "enabled" else "disabled"
            Toast.makeText(this, "Evening reminder $stateText", Toast.LENGTH_SHORT).show()
        }

        binding.cvMorning.setOnClickListener {
            showTimePicker(isMorning = true)
        }

        binding.cvEvening.setOnClickListener {
            showTimePicker(isMorning = false)
        }
    }

    private fun showTimePicker(isMorning: Boolean) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val calendarSelected = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, selectedHour)
                    set(Calendar.MINUTE, selectedMinute)
                }
                val timeFormat = java.text.SimpleDateFormat("hh:mm a", Locale.getDefault())
                val formattedTime = timeFormat.format(calendarSelected.time)

                val prefs = getSharedPreferences("stoic_mind_prefs", Context.MODE_PRIVATE)
                if (isMorning) {
                    prefs.edit().putString("reminder_morning_time", formattedTime).apply()
                    binding.tvMorningTime.text = "Morning Reflection\n$formattedTime"
                } else {
                    prefs.edit().putString("reminder_evening_time", formattedTime).apply()
                    binding.tvEveningTime.text = "Evening Review\n$formattedTime"
                }
                Toast.makeText(this, "Reminder time updated", Toast.LENGTH_SHORT).show()
            },
            hour,
            minute,
            false
        )
        timePickerDialog.show()
    }
}
