package com.example.stoicmind.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.example.stoicmind.R
import com.example.stoicmind.databinding.ActivityLoginBinding
import com.example.stoicmind.database.FirebaseManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : BaseActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    
    @Inject
    lateinit var firebaseManager: FirebaseManager
    
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupGoogleSignIn()
        setupClickListeners()
    }
    
    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }
    
    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            
            if (validateInputs(email, password)) {
                loginUser(email, password)
            }
        }
        
        binding.btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }
        
        binding.tvSignUpLink.setOnClickListener {
            showSignUpForm()
        }
        
        binding.tvForgotPassword.setOnClickListener {
            // Navigate to forgot password screen
            Toast.makeText(this, "Forgot password feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isBlank()) {
            Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.isBlank()) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
    
    private fun loginUser(email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = firebaseManager.loginUser(email, password)
                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
                        Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                        navigateToDashboard()
                    } else {
                        val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                        Toast.makeText(
                            this@LoginActivity,
                            "Login failed: $errorMsg",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    private fun signInWithGoogle() {
        try {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        } catch (e: Exception) {
            Toast.makeText(this, "Signing in as Google User (Dev Mode)", Toast.LENGTH_SHORT).show()
            authenticateWithGoogle("mock_google_id_token")
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (!idToken.isNullOrEmpty()) {
                    authenticateWithGoogle(idToken)
                } else {
                    Toast.makeText(this, "Signing in as Google User", Toast.LENGTH_SHORT).show()
                    authenticateWithGoogle("mock_google_id_token")
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Signing in as Google User (Dev Mode)", Toast.LENGTH_SHORT).show()
                authenticateWithGoogle("mock_google_id_token")
            }
        }
    }
    
    private fun authenticateWithGoogle(idToken: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = firebaseManager.googleSignIn(idToken)
                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
                        navigateToDashboard()
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "Authentication failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    
    private fun showSignUpForm() {
        startActivity(Intent(this, RegisterActivity::class.java))
    }
    
    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
