package com.example.stoicmind.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
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
        setupSearchFilter()
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

    private fun setupSearchFilter() {
        binding.root.findViewById<EditText>(R.id.etSearchExplore)?.addTextChangedListener { text ->
            val query = text.toString().trim()
            if (query.isNotEmpty()) {
                binding.cvResilience.visibility = if ("Resilience".contains(query, ignoreCase = true)) View.VISIBLE else View.GONE
                binding.cvSelfDiscipline.visibility = if ("Self-Discipline".contains(query, ignoreCase = true)) View.VISIBLE else View.GONE
                binding.cvRelationships.visibility = if ("Relationships".contains(query, ignoreCase = true)) View.VISIBLE else View.GONE
                binding.cvMindfulness.visibility = if ("Mindfulness".contains(query, ignoreCase = true)) View.VISIBLE else View.GONE
                binding.cvGratitude.visibility = if ("Gratitude".contains(query, ignoreCase = true)) View.VISIBLE else View.GONE
                binding.cvPersonalDevelopment.visibility = if ("Personal Development".contains(query, ignoreCase = true)) View.VISIBLE else View.GONE
                binding.cvStressManagement.visibility = if ("Stress Management".contains(query, ignoreCase = true)) View.VISIBLE else View.GONE
                binding.cvLeadership.visibility = if ("Leadership".contains(query, ignoreCase = true)) View.VISIBLE else View.GONE
                binding.cvSuccess.visibility = if ("Success".contains(query, ignoreCase = true)) View.VISIBLE else View.GONE
            } else {
                showAllCards()
            }
        }
    }

    private fun showAllCards() {
        binding.cvResilience.visibility = View.VISIBLE
        binding.cvSelfDiscipline.visibility = View.VISIBLE
        binding.cvRelationships.visibility = View.VISIBLE
        binding.cvMindfulness.visibility = View.VISIBLE
        binding.cvGratitude.visibility = View.VISIBLE
        binding.cvPersonalDevelopment.visibility = View.VISIBLE
        binding.cvStressManagement.visibility = View.VISIBLE
        binding.cvLeadership.visibility = View.VISIBLE
        binding.cvSuccess.visibility = View.VISIBLE
    }
}
