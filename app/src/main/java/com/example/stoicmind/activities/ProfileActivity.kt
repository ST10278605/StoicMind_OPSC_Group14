package com.example.stoicmind.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.stoicmind.R
import com.example.stoicmind.database.FirebaseManager
import com.example.stoicmind.databinding.ActivityProfileBinding
import com.example.stoicmind.databinding.DialogAppearanceBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProfileActivity : BaseActivity() {

    private lateinit var languageSpinner: Spinner
    private var isInitialDisplay = true
    private lateinit var binding: ActivityProfileBinding

    @Inject
    lateinit var firebaseManager: FirebaseManager

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                uploadProfileImage(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupClickListeners()
        setupLanguageSpinner()
    }

    private fun setupLanguageSpinner() {
        languageSpinner = binding.languageSpinner

        val languages = arrayOf("English", "Zulu", "Setswana", "Sepedi")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter

        // Set spinner selection using our localized utility state tracker
        val currentLang = LocaleHelper.getLanguage(this)
        val initialSelection = when (currentLang) {
            "zu", "zulu" -> 1
            "tn", "tswana" -> 2
            "nso", "pedi" -> 3
            else -> 0
        }
        languageSpinner.setSelection(initialSelection)

        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isInitialDisplay) {
                    isInitialDisplay = false
                    return
                }

                val selectedLangCode = when (position) {
                    1 -> "zu"
                    2 -> "tn"
                    3 -> "nso"
                    else -> "en"
                }

                if (selectedLangCode != currentLang) {
                    // Update locale config natively
                    LocaleHelper.setLocale(this@ProfileActivity, selectedLangCode)
                    // Restart app to Dashboard to apply language change globally across all screens
                    val intent = Intent(this@ProfileActivity, DashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
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

                binding.tvProfileName.text =
                    user.name.ifEmpty { "Stoic Practitioner" }

                binding.tvProfileEmail.text =
                    user.email.ifEmpty { "practitioner@stoicmind.com" }

                if (user.profileImageUrl.isNotEmpty()) {
                    Glide.with(this@ProfileActivity)
                        .load(user.profileImageUrl)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .circleCrop()
                        .into(binding.ivProfileImage)
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@ProfileActivity,
                    "Could not load profile information",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun uploadProfileImage(imageUri: Uri) {

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            Toast.makeText(
                this,
                "Please log in before uploading a picture",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        binding.btnChangeProfileImage.isEnabled = false
        binding.btnChangeProfileImage.text = "Uploading..."

        lifecycleScope.launch {
            try {
                val result = firebaseManager.uploadProfileImage(
                    userId,
                    imageUri
                )

                result.onSuccess { downloadUrl ->

                    Glide.with(this@ProfileActivity)
                        .load(downloadUrl)
                        .placeholder(R.drawable.ic_profile)
                        .circleCrop()
                        .into(binding.ivProfileImage)

                    Toast.makeText(
                        this@ProfileActivity,
                        "Profile picture uploaded successfully",
                        Toast.LENGTH_LONG
                    ).show()
                }

                result.onFailure { error ->

                    Toast.makeText(
                        this@ProfileActivity,
                        "Upload failed: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {

                Toast.makeText(
                    this@ProfileActivity,
                    "Something went wrong: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()

            } finally {
                binding.btnChangeProfileImage.isEnabled = true
                binding.btnChangeProfileImage.text = "Change Profile Picture"
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

        binding.btnChangeProfileImage.setOnClickListener {
            imagePicker.launch("image/*")
        }

        binding.btnPersonalInfo.setOnClickListener {
            startActivity(Intent(this, PersonalInfoActivity::class.java))
        }

        binding.btnAppearance.setOnClickListener {
            showAppearanceDialog()
        }

        binding.btnNotifications.setOnClickListener {
            val intent = Intent().apply {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    action = android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(
                        android.provider.Settings.EXTRA_APP_PACKAGE,
                        packageName
                    )
                } else {
                    action = "android.settings.APP_NOTIFICATION_SETTINGS"
                    putExtra("app_package", packageName)
                    putExtra("app_uid", applicationInfo.uid)
                }
            }

            startActivity(intent)
            startActivity(Intent(this, RemindersActivity::class.java))
        }

        binding.btnLanguage.setOnClickListener {
            binding.languageSpinner.performClick()
        }

        binding.btnPrivacyPolicy.setOnClickListener {
            val privacyUrl = "https://firebase.google.com/support/privacy"
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(privacyUrl)
            )
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            firebaseManager.logout()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
        }
    }

    private fun showAppearanceDialog() {

        val dialogBinding =
            DialogAppearanceBinding.inflate(LayoutInflater.from(this))

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .create()

        val sharedPreferences =
            getSharedPreferences("stoic_mind_prefs", MODE_PRIVATE)

        val currentTheme =
            sharedPreferences.getString("app_theme", "classic")

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

            val selectedTheme =
                when (dialogBinding.rgThemes.checkedRadioButtonId) {
                    R.id.rbThemeSerene -> "serene"
                    R.id.rbThemeCourage -> "courage"
                    R.id.rbThemeNature -> "nature"
                    else -> "classic"
                }

            sharedPreferences.edit()
                .putString("app_theme", selectedTheme)
                .apply()
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

            val intent = Intent(this, DashboardActivity::class.java)
            intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_NEW_TASK
            )

            startActivity(intent)
            finish()
        }

        dialog.show()
    }
}