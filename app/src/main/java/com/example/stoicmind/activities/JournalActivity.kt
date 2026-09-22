package com.example.stoicmind.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stoicmind.adapters.JournalAdapter
import com.example.stoicmind.database.FirebaseManager
import com.example.stoicmind.databinding.ActivityJournalBinding
import com.example.stoicmind.models.JournalEntry
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityJournalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupTodayDate()
        setupRecyclerView()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        loadJournalEntries()
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

    private fun saveQuickJournalEntry(content: String) {

        val userId = firebaseManager.getCurrentUserId() ?: "guest_user"

        val journalEntry = JournalEntry(
            userId = userId,
            title = "Daily Reflection",
            content = content,
            mood = "",
            date = System.currentTimeMillis(),
            tags = emptyList(),
            isSynced = true
        )

        lifecycleScope.launch {

            val result = firebaseManager.saveJournalEntry(journalEntry)

            if (result.isSuccess) {

                Toast.makeText(
                    this@JournalActivity,
                    "Journal entry saved successfully!",
                    Toast.LENGTH_SHORT
                ).show()

                binding.etJournalContent.text?.clear()
                loadJournalEntries()

            } else {

                Toast.makeText(
                    this@JournalActivity,
                    "Unable to save journal entry. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun loadJournalEntries() {
        val userId = firebaseManager.getCurrentUserId() ?: "guest_user"

        lifecycleScope.launch {
            val entries = firebaseManager.getJournalEntries(userId)

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

    private fun showEntryDetailsDialog(entry: JournalEntry) {
        val sdf = SimpleDateFormat("MMMM d, yyyy • hh:mm a", Locale.getDefault())
        val dateStr = sdf.format(Date(entry.date))
        val moodStr = if (entry.mood.isNotEmpty()) "Mood: ${entry.mood}\n\n" else ""
        val message = "Date: $dateStr\n$moodStr${entry.content}"

        AlertDialog.Builder(this)
            .setTitle(entry.title.ifEmpty { "Daily Reflection" })
            .setMessage(message)
            .setPositiveButton("Close", null)
            .show()
    }
}
