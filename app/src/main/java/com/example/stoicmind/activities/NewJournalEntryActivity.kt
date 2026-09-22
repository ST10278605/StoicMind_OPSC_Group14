package com.example.stoicmind.activities

import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.stoicmind.database.FirebaseManager
import com.example.stoicmind.databinding.ActivityJournalNewEntryBinding
import com.example.stoicmind.models.JournalEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NewJournalEntryActivity : BaseActivity() {

    private lateinit var binding: ActivityJournalNewEntryBinding

    @Inject
    lateinit var firebaseManager: FirebaseManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityJournalNewEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarInclude.toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "New Journal Entry"

        binding.toolbarInclude.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {

        binding.btnSave.setOnClickListener {

            val title = binding.etTitle.text.toString().trim()
            val content = binding.etContent.text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please enter a title",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (content.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please write something in your journal",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            saveJournalEntry(title, content)
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun saveJournalEntry(title: String, content: String) {

        val userId = firebaseManager.getCurrentUserId() ?: "guest_user"

        val selectedMoodId = binding.rgMood.checkedRadioButtonId

        val mood = if (selectedMoodId != -1) {
            findViewById<RadioButton>(selectedMoodId)?.text?.toString() ?: ""
        } else {
            ""
        }

        val journalEntry = JournalEntry(
            userId = userId,
            title = title,
            content = content,
            mood = mood,
            date = System.currentTimeMillis(),
            tags = emptyList(),
            isSynced = true
        )

        lifecycleScope.launch {

            val result = firebaseManager.saveJournalEntry(journalEntry)

            if (result.isSuccess) {

                Toast.makeText(
                    this@NewJournalEntryActivity,
                    "Journal entry saved successfully!",
                    Toast.LENGTH_SHORT
                ).show()

                finish()

            } else {

                Toast.makeText(
                    this@NewJournalEntryActivity,
                    "Unable to save journal entry. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
