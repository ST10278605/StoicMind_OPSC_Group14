package com.example.stoicmind.activities

import android.os.Bundle
import android.widget.Toast
import com.example.stoicmind.databinding.ActivityRemindersBinding
import com.example.stoicmind.notifications.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RemindersActivity : BaseActivity() {

    private lateinit var binding: ActivityRemindersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRemindersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupClickListeners()
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
