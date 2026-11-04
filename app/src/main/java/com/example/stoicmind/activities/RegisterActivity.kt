package com.example.stoicmind.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.example.stoicmind.databinding.ActivityRegisterBinding
import com.example.stoicmind.database.FirebaseManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class RegisterActivity : BaseActivity() {

    private lateinit var binding: ActivityRegisterBinding

    @Inject
    lateinit var firebaseManager: FirebaseManager

    companion object {
        private const val TAG = "RegisterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()

            Log.d(TAG, "Create Account clicked. Name: $name, Email: $email")

            if (validateInputs(name, email, password)) {
                registerUser(name, email, password)
            }
        }

        binding.tvLoginLink.setOnClickListener {
            finish()
        }
    }

    private fun validateInputs(name: String, email: String, password: String): Boolean {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }
        if (!hasSpecialChar) {
            Toast.makeText(this, "Please add a special character to your password (after the 6 required letters or numbers)", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun registerUser(name: String, email: String, password: String) {
        binding.btnRegister.isEnabled = false
        Toast.makeText(this, "Creating account...", Toast.LENGTH_SHORT).show()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = firebaseManager.registerUser(email, password, name)
                withContext(Dispatchers.Main) {
                    binding.btnRegister.isEnabled = true
                    if (result.isSuccess) {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Account created successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        navigateToDashboard()
                    } else {
                        val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                        Toast.makeText(
                            this@RegisterActivity,
                            "Registration failed: $errorMsg",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.btnRegister.isEnabled = true
                    Log.e(TAG, "Registration error", e)
                    Toast.makeText(this@RegisterActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
