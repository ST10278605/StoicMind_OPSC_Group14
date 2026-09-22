package com.example.stoicmind.activities

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.stoicmind.database.FirebaseManager
import com.example.stoicmind.databinding.ActivityMoodCheckinBinding
import com.example.stoicmind.models.JournalEntry
import com.example.stoicmind.models.MoodCheckIn
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MoodCheckInActivity : BaseActivity() {

    private lateinit var binding: ActivityMoodCheckinBinding
    private var selectedMood: String = "😌 Calm"

    @Inject
    lateinit var firebaseManager: FirebaseManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodCheckinBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupMoodSelection()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarInclude.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Mood Check-In"
        binding.toolbarInclude.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupMoodSelection() {
        val moodButtons = listOf(
            binding.btnMoodHappy,
            binding.btnMoodCalm,
            binding.btnMoodNeutral,
            binding.btnMoodSad,
            binding.btnMoodAngry,
            binding.btnMoodAnxious
        )

        moodButtons.forEach { btn ->
            btn.setOnClickListener {
                selectedMood = btn.text.toString()
                Toast.makeText(this, "Selected: $selectedMood", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSaveMood.setOnClickListener {
            val note = binding.etMoodNote.text.toString().trim()
            val userId = firebaseManager.getCurrentUserId() ?: "guest_user"

            val moodCheckIn = MoodCheckIn(
                userId = userId,
                mood = selectedMood,
                note = note,
                date = System.currentTimeMillis(),
                isSynced = true
            )

            val journalEntry = JournalEntry(
                userId = userId,
                title = "Mood Check-In: $selectedMood",
                content = if (note.isNotEmpty()) "Mood Note: $note" else "Checked in feeling $selectedMood.",
                mood = selectedMood,
                date = System.currentTimeMillis(),
                isSynced = true
            )

            lifecycleScope.launch {
                firebaseManager.saveMood(moodCheckIn)
                firebaseManager.saveJournalEntry(journalEntry)
                Toast.makeText(this@MoodCheckInActivity, "Mood check-in saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
