package com.example.stoicmind.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stoicmind.adapters.JournalAdapter
import com.example.stoicmind.database.FirebaseManager
import com.example.stoicmind.databinding.ActivityJournalBinding
import com.example.stoicmind.models.JournalEntry
import com.example.stoicmind.repository.SyncRepository
import com.example.stoicmind.repository.SyncStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class JournalActivity : BaseActivity() {

    private lateinit var binding: ActivityJournalBinding
    private lateinit var journalAdapter: JournalAdapter

    @Inject
    lateinit var firebaseManager: FirebaseManager

    @Inject
    lateinit var syncRepository: SyncRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityJournalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupTodayDate()
        setupRecyclerView()
        setupSyncBanner()
        setupClickListeners()
        observeData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarInclude.toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Journal"

        binding.toolbarInclude.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupTodayDate() {
        val sdf = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
        binding.tvTodayDate.text = sdf.format(Date())
    }

    private fun setupRecyclerView() {
        journalAdapter = JournalAdapter(emptyList()) { entry ->
            showEntryDetailsDialog(entry)
        }
        binding.rvJournalEntries.apply {
            layoutManager = LinearLayoutManager(this@JournalActivity)
            adapter = journalAdapter
        }
    }

    private fun setupSyncBanner() {
        binding.syncBannerInclude.btnSyncNow.setOnClickListener {
            lifecycleScope.launch {
                Toast.makeText(this@JournalActivity, "Syncing with cloud...", Toast.LENGTH_SHORT).show()
                syncRepository.syncAll()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSaveJournalEntry.setOnClickListener {
            val content = binding.etJournalContent.text.toString().trim()

            if (content.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please write something before saving.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            saveQuickJournalEntry(content)
        }

        binding.btnNewEntry.setOnClickListener {
            startActivity(Intent(this, NewJournalEntryActivity::class.java))
        }
    }

    private fun observeData() {
        val userId = firebaseManager.getCurrentUserId() ?: "guest_user"

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    syncRepository.getJournalEntriesFlow(userId).collect { entries ->
                        if (entries.isNotEmpty()) {
                            journalAdapter.updateEntries(entries)
                            binding.rvJournalEntries.visibility = View.VISIBLE
                            binding.tvEmptyJournal.visibility = View.GONE
                        } else {
                            journalAdapter.updateEntries(emptyList())
                            binding.rvJournalEntries.visibility = View.GONE
                            binding.tvEmptyJournal.visibility = View.VISIBLE
                        }
                    }
                }

                launch {
                    syncRepository.syncState.collect { syncState ->
                        updateSyncStateUI(syncState.status, syncState.unsyncedJournalCount, syncState.message)
                    }
                }
            }
        }
    }

    private fun updateSyncStateUI(status: SyncStatus, unsyncedCount: Int, message: String) {
        with(binding.syncBannerInclude) {
            when (status) {
                SyncStatus.OFFLINE -> {
                    cardSyncBanner.setCardBackgroundColor(Color.parseColor("#FFF3E0"))
                    tvSyncStatus.setTextColor(Color.parseColor("#E65100"))
                    ivSyncIcon.setColorFilter(Color.parseColor("#E65100"))
                    tvSyncStatus.text = if (unsyncedCount > 0) {
                        "Offline Mode • $unsyncedCount unsynced entry(ies) saved in Room"
                    } else {
                        "Offline Mode • All local Room entries up to date"
                    }
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
                    tvSyncStatus.text = "Online • All entries synced with Firestore"
                    btnSyncNow.visibility = View.VISIBLE
                }
                SyncStatus.ERROR -> {
                    cardSyncBanner.setCardBackgroundColor(Color.parseColor("#FFEBEE"))
                    tvSyncStatus.setTextColor(Color.parseColor("#C62828"))
                    ivSyncIcon.setColorFilter(Color.parseColor("#C62828"))
                    tvSyncStatus.text = "Sync failed: $message"
                    btnSyncNow.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun saveQuickJournalEntry(content: String) {
        val userId = firebaseManager.getCurrentUserId() ?: "guest_user"

        val journalEntry = JournalEntry(
            userId = userId,
            title = "Daily Reflection",
            content = content,
            mood = "",
            date = System.currentTimeMillis(),
            tags = emptyList(),
            isSynced = false
        )

        lifecycleScope.launch {
            val result = syncRepository.saveJournalEntry(journalEntry)

            if (result.isSuccess) {
                Toast.makeText(
                    this@JournalActivity,
                    "Journal entry saved to Room DB!",
                    Toast.LENGTH_SHORT
                ).show()

                binding.etJournalContent.text?.clear()
            } else {
                Toast.makeText(
                    this@JournalActivity,
                    "Unable to save journal entry. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showEntryDetailsDialog(entry: JournalEntry) {
        val sdf = SimpleDateFormat("MMMM d, yyyy • hh:mm a", Locale.getDefault())
        val dateStr = sdf.format(Date(entry.date))
        val moodStr = if (entry.mood.isNotEmpty()) "Mood: ${entry.mood}\n\n" else ""
        val syncStr = if (entry.isSynced) "Status: Synced to Cloud\n" else "Status: Saved Locally (Room DB)\n"
        val message = "Date: $dateStr\n$syncStr$moodStr${entry.content}"

        AlertDialog.Builder(this)
            .setTitle(entry.title.ifEmpty { "Daily Reflection" })
            .setMessage(message)
            .setPositiveButton("Close", null)
            .show()
    }
}
