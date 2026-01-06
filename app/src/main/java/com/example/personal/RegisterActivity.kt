package com.example.personal

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.personal.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()

        binding.buttonSignUp.setOnClickListener {
            val username = binding.signupUsername.text.toString().trim()
            val password = binding.signupPassword.text.toString().trim()
            val genderId = binding.signupGender.checkedRadioButtonId
            val gender = findViewById<RadioButton>(genderId).text.toString()
            val email = binding.signupEmail.text.toString().trim()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Invalid email pattern!", Toast.LENGTH_SHORT).show()
            } else if(username.isNotEmpty() && password.isNotEmpty() && email.isNotEmpty()) {
                registerUser(email, password, username, gender, role = "user")
            } else {
                Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.gotoLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser(email: String, password: String, username: String, gender: String, role: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    val userId = firebaseUser?.uid
                    // Store additional user information
                    if (userId != null) {
                        val userData = mapOf("username" to username, "gender" to gender, "role" to role)
                        firebaseDatabase.reference.child("users").child(userId).setValue(userData)
                    }
                    Toast.makeText(this, "Signup Successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Signup Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}
