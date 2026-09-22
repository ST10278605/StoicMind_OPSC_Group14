package com.example.stoicmind.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.stoicmind.R
import com.example.stoicmind.databinding.ActivityDashboardBinding
import com.example.stoicmind.database.FirebaseManager
import com.example.stoicmind.models.Quote
import com.example.stoicmind.api.RetrofitClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DashboardActivity : BaseActivity() {
    
    private lateinit var binding: ActivityDashboardBinding
    
    @Inject
    lateinit var firebaseManager: FirebaseManager
    
    private var currentQuote: Quote? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupClickListeners()
        setupBottomNavigation()
        updateDateDisplay()
        loadDailyQuote()
        loadUserData()
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
    }

    private fun updateDateDisplay() {
        val sdf = java.text.SimpleDateFormat("EEEE, MMMM d, yyyy", java.util.Locale.getDefault())
        binding.tvDate.text = sdf.format(java.util.Date())
    }
    
    private fun setupClickListeners() {
        binding.btnReflectNow.setOnClickListener {
            startActivity(Intent(this, DailyReflectionActivity::class.java))
        }
        
        binding.btnJournal.setOnClickListener {
            startActivity(Intent(this, JournalActivity::class.java))
        }
        
        binding.btnMood.setOnClickListener {
            startActivity(Intent(this, MoodCheckInActivity::class.java))
        }
        
        binding.btnHabits.setOnClickListener {
            startActivity(Intent(this, HabitsActivity::class.java))
        }
        
        binding.btnExplore.setOnClickListener {
            startActivity(Intent(this, ExploreActivity::class.java))
        }

        binding.btnAchievements.setOnClickListener {
            startActivity(Intent(this, AchievementsActivity::class.java))
        }

        binding.btnReminders.setOnClickListener {
            startActivity(Intent(this, RemindersActivity::class.java))
        }
    }
    
    private fun setupBottomNavigation() {
        binding.bottomNav.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    true
                }
                R.id.nav_explore -> {
                    startActivity(Intent(this, ExploreActivity::class.java))
                    true
                }
                R.id.nav_journal -> {
                    startActivity(Intent(this, JournalActivity::class.java))
                    true
                }
                R.id.nav_community -> {
                    Toast.makeText(this, "Community coming soon", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
        
        binding.bottomNav.selectedItemId = R.id.nav_home
    }

    private fun loadDailyQuote() {
        lifecycleScope.launch {
            try {
                val quote = RetrofitClient.apiService.getRandomQuote()
                currentQuote = quote
                updateQuoteUI(quote)
            } catch (e: Exception) {
                binding.tvDailyQuote.text = "The obstacle is the way."
                binding.tvQuoteAuthor.text = "- Marcus Aurelius"
            }
        }
    }
    
    private fun updateQuoteUI(quote: Quote) {
        binding.tvDailyQuote.text = quote.text
        binding.tvQuoteAuthor.text = "- ${quote.author}"
    }
    
    private fun loadUserData() {
        val userId = firebaseManager.getCurrentUserId()
        if (userId != null) {
            lifecycleScope.launch {
                try {
                    val user = firebaseManager.getUser(userId)
                    binding.tvGreeting.text = "Good Morning, ${user.name.ifEmpty { "Stoic" }}"
                } catch (e: Exception) {
                    binding.tvGreeting.text = "Good Morning, Stoic"
                }
            }
        }
    }
}