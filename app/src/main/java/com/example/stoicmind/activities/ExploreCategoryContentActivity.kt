package com.example.stoicmind.activities

import android.os.Bundle
import com.example.stoicmind.databinding.ActivityExploreCategoryContentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExploreCategoryContentActivity : BaseActivity() {

    private lateinit var binding: ActivityExploreCategoryContentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExploreCategoryContentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val categoryName = intent.getStringExtra("category_name") ?: "Wisdom"
        
        setupToolbar(categoryName)
    }

    private fun setupToolbar(title: String) {
        setSupportActionBar(binding.toolbarInclude.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = title
        binding.toolbarInclude.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
}