package com.example.stoicmind.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.stoicmind.R
import com.example.stoicmind.databinding.ActivityExploreBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExploreActivity : BaseActivity() {

    private lateinit var binding: ActivityExploreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExploreBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarInclude.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Explore Wisdom"
        binding.toolbarInclude.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        binding.root.findViewById<android.view.View>(R.id.etSearchExplore)?.let { search ->
            search.setOnClickListener {
                android.widget.Toast.makeText(this, "Search feature coming soon", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        val clickListener = { category: String ->
            val intent = Intent(this, ExploreCategoryContentActivity::class.java)
            intent.putExtra("category_name", category)
            startActivity(intent)
        }

        binding.cvResilience.setOnClickListener { clickListener("Resilience") }
        binding.cvSelfDiscipline.setOnClickListener { clickListener("Self-Discipline") }
        binding.cvRelationships.setOnClickListener { clickListener("Relationships") }
        binding.cvMindfulness.setOnClickListener { clickListener("Mindfulness") }
        binding.cvGratitude.setOnClickListener { clickListener("Gratitude") }
        binding.cvPersonalDevelopment.setOnClickListener { clickListener("Personal Development") }
        binding.cvStressManagement.setOnClickListener { clickListener("Stress Management") }
        binding.cvLeadership.setOnClickListener { clickListener("Leadership") }
        binding.cvSuccess.setOnClickListener { clickListener("Success") }
    }
}