package com.example.personal

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.personal.AdminController.HomeActivity
import com.example.personal.databinding.ActivityPlashBinding

class PlashActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlashBinding

    private val splashScreenDuration: Long = 3000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPlashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            val useremail = intent.getStringExtra("useremail")
            val username = intent.getStringExtra("username")
            val userId = intent.getStringExtra("userId")
            val role = intent.getStringExtra("role")
            if (role == null) {
                val intent = Intent(this@PlashActivity, MainActivity2::class.java)
                intent.putExtra("useremail", useremail)
                intent.putExtra("username", username)
                intent.putExtra("userId", userId)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this@PlashActivity, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, splashScreenDuration)


    }
}