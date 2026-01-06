package com.example.personal

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.personal.AdminController.HomeActivity
import com.example.personal.Data.UserData
import com.example.personal.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.buttonLogin.setOnClickListener {
            val email = binding.loginEmail.text.toString().trim()
            val password = binding.loginPassword.text.toString().trim()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Invalid email pattern!", Toast.LENGTH_SHORT).show()
            } else if(email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            }
            else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.gotoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        val messenger = intent.getStringExtra("messenger")
        if (messenger != null) {
            Toast.makeText(this@LoginActivity, messenger, Toast.LENGTH_SHORT).show()
        }

    }

    private fun loginUser(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Đăng nhập thành công, lấy thông tin về người dùng từ Firebase
                    val user = firebaseAuth.currentUser
                    val userId = user?.uid
                    val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId!!)
                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val userData = dataSnapshot.getValue(UserData::class.java)
                            if (userData != null && userData.role == "user") {
                                // Lấy thành công thông tin về người dùng, chuyển sang MainActivity và truyền thông tin cần thiết
                                val intent = Intent(this@LoginActivity, PlashActivity::class.java)
                                intent.putExtra("useremail", user?.email)
                                intent.putExtra("username", userData.username)
                                intent.putExtra("userId", userId)
                                //lưu email gửi đến các trang xem có đăng nhập chưa
                                val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                                val editor = sharedPreferences.edit()
                                editor.putString("email", "example@example.com")
                                editor.apply()
                                startActivity(intent)
                                finish()
                            } else {
                                //nếu là admin thì đi hướng khác
                                val intent = Intent(this@LoginActivity, PlashActivity::class.java)
                                intent.putExtra("role", userData?.role)
                                startActivity(intent)
                                finish()
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Xử lý lỗi nếu có
                            Toast.makeText(this@LoginActivity, "Failed to fetch user data: ${databaseError.message}", Toast.LENGTH_LONG).show()
                        }
                    })
                    Toast.makeText(this@LoginActivity, "Login Successfull!", Toast.LENGTH_LONG).show()
                } else {
                    // Xử lý khi đăng nhập thất bại
                    Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

}