package com.example.stoicmind.activities

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.stoicmind.database.FirebaseManager
import com.example.stoicmind.databinding.ActivityMoodCheckinBinding
import com.example.stoicmind.models.JournalEntry
import com.example.stoicmind.models.MoodCheckIn
import com.example.stoicmind.repository.SyncRepository
import com.example.stoicmind.repository.SyncStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MoodCheckInActivity : BaseActivity() {

    private lateinit var binding: ActivityMoodCheckinBinding
    private var selectedMood: String = "😌 Calm"

    @Inject
    lateinit var firebaseManager: FirebaseManager

    @Inject
    lateinit var syncRepository: SyncRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodCheckinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupMoodSelection()
        setupSyncBanner()
        setupClickListeners()
        observeSyncState()
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

    private fun setupSyncBanner() {
        binding.syncBannerInclude.btnSyncNow.setOnClickListener {
            lifecycleScope.launch {
                Toast.makeText(this@MoodCheckInActivity, "Syncing data...", Toast.LENGTH_SHORT).show()
                syncRepository.syncAll()
            }
        }
    }

    private fun observeSyncState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                syncRepository.syncState.collect { syncState ->
                    with(binding.syncBannerInclude) {
                        when (syncState.status) {
                            SyncStatus.OFFLINE -> {
                                cardSyncBanner.setCardBackgroundColor(Color.parseColor("#FFF3E0"))
                                tvSyncStatus.setTextColor(Color.parseColor("#E65100"))
                                ivSyncIcon.setColorFilter(Color.parseColor("#E65100"))
                                tvSyncStatus.text = "Offline Mode • Mood check-ins save locally to Room"
                                btnSyncNow.visibility = View.VISIBLE
                            }
                            SyncStatus.SYNCING -> {
                                cardSyncBanner.setCardBackgroundColor(Color.parseColor("#E3F2FD"))
                                tvSyncStatus.setTextColor(Color.parseColor("#1565C0"))
                                ivSyncIcon.setColorFilter(Color.parseColor("#1565C0"))
                                tvSyncStatus.text = "Syncing with cloud database..."
                                btnSyncNow.visibility = View.GONE
                            }
                            SyncStatus.SYNCED -> {
                                cardSyncBanner.setCardBackgroundColor(Color.parseColor("#E8F5E9"))
                                tvSyncStatus.setTextColor(Color.parseColor("#2E7D32"))
                                ivSyncIcon.setColorFilter(Color.parseColor("#2E7D32"))
                                tvSyncStatus.text = "Online • Synced with cloud"
                                btnSyncNow.visibility = View.VISIBLE
                            }
                            SyncStatus.ERROR -> {
                                cardSyncBanner.setCardBackgroundColor(Color.parseColor("#FFEBEE"))
                                tvSyncStatus.setTextColor(Color.parseColor("#C62828"))
                                ivSyncIcon.setColorFilter(Color.parseColor("#C62828"))
                                tvSyncStatus.text = "Sync status: ${syncState.message}"
                                btnSyncNow.visibility = View.VISIBLE
                            }
                        }
                    }
                }
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
                isSynced = false
            )

            val journalEntry = JournalEntry(
                userId = userId,
                title = "Mood Check-In: $selectedMood",
                content = if (note.isNotEmpty()) "Mood Note: $note" else "Checked in feeling $selectedMood.",
                mood = selectedMood,
                date = System.currentTimeMillis(),
                isSynced = false
            )

            lifecycleScope.launch {
                syncRepository.saveMoodCheckIn(moodCheckIn)
                syncRepository.saveJournalEntry(journalEntry)
                Toast.makeText(this@MoodCheckInActivity, "Mood check-in saved to Room DB!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
