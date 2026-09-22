package com.example.stoicmind.activities

import android.os.Bundle
import com.example.stoicmind.activities.BaseActivity
import com.example.stoicmind.databinding.ActivityRemindersBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RemindersActivity : BaseActivity() {

    private lateinit var binding: ActivityRemindersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRemindersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarInclude.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Reminders"
        binding.toolbarInclude.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
}