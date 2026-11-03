package com.example.stoicmind.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.example.stoicmind.R
import com.example.stoicmind.database.FirebaseManager
import com.example.stoicmind.databinding.ActivityProfileBinding
import com.example.stoicmind.databinding.DialogAppearanceBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityProfileBinding

    @Inject
    lateinit var firebaseManager: FirebaseManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
    }

    private fun loadUserData() {
        val userId = firebaseManager.getCurrentUserId() ?: return
        lifecycleScope.launch {
            try {
                val user = firebaseManager.getUser(userId)
                binding.tvProfileName.text = user.name.ifEmpty { "Stoic Practitioner" }
                binding.tvProfileEmail.text = user.email.ifEmpty { "practitioner@stoicmind.com" }
            } catch (e: Exception) {
                // Keep default layout text
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarInclude.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Profile & Settings"
        binding.toolbarInclude.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        binding.btnPersonalInfo.setOnClickListener {
            startActivity(Intent(this, PersonalInfoActivity::class.java))
        }

        binding.btnAppearance.setOnClickListener {
            showAppearanceDialog()
        }

        binding.btnNotifications.setOnClickListener {
            startActivity(Intent(this, RemindersActivity::class.java))
        }

        binding.btnPrivacyPolicy.setOnClickListener {
            val privacyUrl = "https://firebase.google.com/support/privacy"
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(privacyUrl))
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            firebaseManager.logout()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun showAppearanceDialog() {
        val dialogBinding = DialogAppearanceBinding.inflate(LayoutInflater.from(this))
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .create()

        val sharedPreferences = getSharedPreferences("stoic_mind_prefs", MODE_PRIVATE)
        val currentTheme = sharedPreferences.getString("app_theme", "classic")

        when (currentTheme) {
            "serene" -> dialogBinding.rbThemeSerene.isChecked = true
            "courage" -> dialogBinding.rbThemeCourage.isChecked = true
            "nature" -> dialogBinding.rbThemeNature.isChecked = true
            else -> dialogBinding.rbThemeClassic.isChecked = true
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnApply.setOnClickListener {
            val selectedTheme = when (dialogBinding.rgThemes.checkedRadioButtonId) {
                R.id.rbThemeSerene -> "serene"
                R.id.rbThemeCourage -> "courage"
                R.id.rbThemeNature -> "nature"
                else -> "classic"
            }

            sharedPreferences.edit().putString("app_theme", selectedTheme).apply()

            val userId = firebaseManager.getCurrentUserId()
            if (userId != null) {
                lifecycleScope.launch {
                    try {
                        val user = firebaseManager.getUser(userId)
                        firebaseManager.saveUser(user.copy(theme = selectedTheme))
                    } catch (_: Exception) {
                        // Ignore network sync issues
                    }
                }
            }

            dialog.dismiss()

            // Restart the app from the Dashboard to apply the theme globally
            val intent = Intent(this, DashboardActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        dialog.show()
    }
}
