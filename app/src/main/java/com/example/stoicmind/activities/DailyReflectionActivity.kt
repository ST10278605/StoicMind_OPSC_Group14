package com.example.stoicmind.activities

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.stoicmind.database.FirebaseManager
import com.example.stoicmind.databinding.ActivityDailyReflectionBinding
import com.example.stoicmind.models.JournalEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DailyReflectionActivity : BaseActivity() {

    private lateinit var binding: ActivityDailyReflectionBinding

    @Inject
    lateinit var firebaseManager: FirebaseManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDailyReflectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarInclude.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Daily Reflection"
        binding.toolbarInclude.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        binding.btnCompletePractice.setOnClickListener {
            val focusText = binding.etFocus.text.toString().trim()
            val wentWellText = binding.etWentWell.text.toString().trim()
            val doBetterText = binding.etDoBetter.text.toString().trim()

            val contentBuilder = StringBuilder()
            if (focusText.isNotEmpty()) {
                contentBuilder.append("Today's Focus:\n").append(focusText).append("\n\n")
            }
            if (wentWellText.isNotEmpty()) {
                contentBuilder.append("What Went Well:\n").append(wentWellText).append("\n\n")
            }
            if (doBetterText.isNotEmpty()) {
                contentBuilder.append("What To Improve:\n").append(doBetterText)
            }

            val finalContent = contentBuilder.toString().trim()

            if (finalContent.isEmpty()) {
                Toast.makeText(this, "Please enter at least one reflection focus or review.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = firebaseManager.getCurrentUserId() ?: "guest_user"
            val journalEntry = JournalEntry(
                userId = userId,
                title = "Daily Reflection",
                content = finalContent,
                date = System.currentTimeMillis(),
                isSynced = true
            )

            lifecycleScope.launch {
                firebaseManager.saveJournalEntry(journalEntry)
                Toast.makeText(this@DailyReflectionActivity, "Daily practice completed! Reflection saved.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
