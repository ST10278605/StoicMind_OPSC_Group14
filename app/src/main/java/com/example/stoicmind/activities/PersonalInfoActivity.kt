package com.example.stoicmind.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.stoicmind.database.FirebaseManager
import com.example.stoicmind.databinding.ActivityPersonalInfoBinding
import com.example.stoicmind.models.User
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class PersonalInfoActivity : BaseActivity() {

    private lateinit var binding: ActivityPersonalInfoBinding

    @Inject
    lateinit var firebaseManager: FirebaseManager

    private var currentUser: User? = null
    private val virtues = listOf("Wisdom", "Justice", "Courage", "Temperance")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupVirtueSpinner()
        loadUserData()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarInclude.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Personal Information"
        binding.toolbarInclude.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupVirtueSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, virtues)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerVirtue.adapter = adapter
    }

    private fun loadUserData() {
        val userId = firebaseManager.getCurrentUserId() ?: return

        lifecycleScope.launch {
            try {
                currentUser = firebaseManager.getUser(userId)
                displayUserData()
            } catch (e: Exception) {
                Toast.makeText(this@PersonalInfoActivity, "Error loading data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayUserData() {
        currentUser?.let { user ->
            binding.etName.setText(user.name)
            binding.etEmail.setText(user.email)
            
            // Format Join Date
            val sdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())
            binding.tvJoinedDate.text = sdf.format(Date(user.createdAt))
            
            // Set Rank based on level
            binding.tvRank.text = when {
                user.level >= 20 -> "Stoic Master"
                user.level >= 10 -> "Philosopher"
                user.level >= 5 -> "Practitioner"
                else -> "Initiate"
            }

            // Set Spinner Selection
            val virtueIndex = virtues.indexOf(user.primaryVirtue)
            if (virtueIndex != -1) {
                binding.spinnerVirtue.setSelection(virtueIndex)
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSaveChanges.setOnClickListener {
            saveChanges()
        }

        binding.btnChangePassword.setOnClickListener {
            requestPasswordReset()
        }

        binding.btnDeleteAccount.setOnClickListener {
            confirmDeleteAccount()
        }
    }

    private fun saveChanges() {
        val newName = binding.etName.text.toString().trim()
        val selectedVirtue = binding.spinnerVirtue.selectedItem.toString()

        if (newName.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedUser = currentUser?.copy(
            name = newName,
            primaryVirtue = selectedVirtue,
            updatedAt = System.currentTimeMillis()
        ) ?: return

        lifecycleScope.launch {
            try {
                firebaseManager.saveUser(updatedUser)
                currentUser = updatedUser
                Toast.makeText(this@PersonalInfoActivity, "Changes saved successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@PersonalInfoActivity, "Failed to save changes", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestPasswordReset() {
        val email = currentUser?.email

        if (email.isNullOrEmpty()) {
            Toast.makeText(this, "No email found for this account", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Sending reset request for $email...", Toast.LENGTH_SHORT).show()

        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Reset email sent to $email", Toast.LENGTH_LONG).show()
                } else {
                    val error = task.exception?.message ?: "Unknown error occurred"
                    Toast.makeText(this, "Failed: $error", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun confirmDeleteAccount() {
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your journey? This action is irreversible and all your progress will be lost.")
            .setPositiveButton("Delete") { _, _ ->
                // In a real app, you'd handle Firebase User deletion here
                Toast.makeText(this, "Account deletion requested", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}