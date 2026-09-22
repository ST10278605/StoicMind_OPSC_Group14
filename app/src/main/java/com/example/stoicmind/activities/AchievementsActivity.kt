package com.example.stoicmind.activities

import android.graphics.Color
import android.os.Bundle
import com.example.stoicmind.R
import com.example.stoicmind.databinding.ActivityAchievementsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AchievementsActivity : BaseActivity() {

    private lateinit var binding: ActivityAchievementsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAchievementsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupBadges()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarInclude.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Progress"
        binding.toolbarInclude.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupBadges() {
        // Badge 1: Daily Writer
        binding.badge1.tvBadgeName.text = "Daily Writer"
        binding.badge1.ivBadgeIcon.setImageResource(R.drawable.ic_journal)
        binding.badge1.cvBadgeIcon.setCardBackgroundColor(Color.parseColor("#EAF2F8"))

        // Badge 2: Stoic Master
        binding.badge2.tvBadgeName.text = "Stoic Master"
        binding.badge2.ivBadgeIcon.setImageResource(R.mipmap.ic_launcher)
        binding.badge2.cvBadgeIcon.setCardBackgroundColor(Color.parseColor("#FAD7A0"))

        // Badge 3: Fire Starter
        binding.badge3.tvBadgeName.text = "Fire Starter"
        binding.badge3.ivBadgeIcon.setImageResource(R.drawable.ic_habits)
        binding.badge3.cvBadgeIcon.setCardBackgroundColor(Color.parseColor("#F5B7B1"))

        // Badge 4: Explorer
        binding.badge4.tvBadgeName.text = "Explorer"
        binding.badge4.ivBadgeIcon.setImageResource(R.drawable.ic_explore)
        binding.badge4.cvBadgeIcon.setCardBackgroundColor(Color.parseColor("#D1F2EB"))

        // Badge 5: Peace Maker
        binding.badge5.tvBadgeName.text = "Peace Maker"
        binding.badge5.ivBadgeIcon.setImageResource(R.drawable.ic_mood)
        binding.badge5.cvBadgeIcon.setCardBackgroundColor(Color.parseColor("#D6EAF8"))
        binding.badge5.tvBadgeStatus.text = "Locked"
        binding.badge5.tvBadgeStatus.setTextColor(Color.GRAY)
        binding.badge5.cvBadgeIcon.alpha = 0.5f

        // Badge 6: Disciplined
        binding.badge6.tvBadgeName.text = "Disciplined"
        binding.badge6.ivBadgeIcon.setImageResource(R.drawable.ic_habits)
        binding.badge6.cvBadgeIcon.setCardBackgroundColor(Color.parseColor("#FCF3CF"))
        binding.badge6.tvBadgeStatus.text = "Locked"
        binding.badge6.tvBadgeStatus.setTextColor(Color.GRAY)
        binding.badge6.cvBadgeIcon.alpha = 0.5f
    }
}