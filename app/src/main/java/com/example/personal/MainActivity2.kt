package com.example.personal

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.personal.databinding.ActivityMain2Binding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity2 : AppCompatActivity() {

    private lateinit var bottomNavi: BottomNavigationView
    private lateinit var binding: ActivityMain2Binding
    private lateinit var firebaseAuth: FirebaseAuth
    private var useremail: String? = null
    private var username: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMain2Binding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()

        setContentView(binding.root)

        bottomNavi = findViewById(R.id.bottomNavigation)

        bottomNavi.setOnItemSelectedListener { MenuItem ->
            when (MenuItem.itemId) {
                R.id.home -> {
                    replaceFragment(HomeFragment())
                    true
                }

                R.id.profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }

                R.id.favorite -> {
                    replaceFragment(FavoriteFragment())
                    true
                }

                else -> false
            }
        }
        replaceFragment(HomeFragment())

        useremail = intent.getStringExtra("useremail")
        username = intent.getStringExtra("username")
        val userId = intent.getStringExtra("userId")
        binding.fab.setOnClickListener {
           if (username == null && useremail == null && userId == null) {
               AlertDialog.Builder(this).setTitle("Failed").setMessage("LOGIN PLEASE!").setPositiveButton(android.R.string.ok, null)
           } else {
               val intent = Intent(this@MainActivity2, AddpostActivity::class.java)
               intent.putExtra("useremail", useremail)
               intent.putExtra("username", username)
               intent.putExtra("userId", userId)
               startActivity(intent)
           }
        }
        // Load the appropriate fragment based on the intent
        val fragmentToLoad = intent.getStringExtra("ProfileFragment")
        if (fragmentToLoad == "ProfileFragment") {
            replaceFragment(ProfileFragment())
            binding.bottomNavigation.selectedItemId = R.id.profile
        } else {
            // Load default fragment
            replaceFragment(HomeFragment())
            binding.bottomNavigation.selectedItemId = R.id.home
        }
    }

    override fun onStop() {
        super.onStop()
        // Đăng xuất Firebase
        firebaseAuth.signOut()
        // Xóa thông tin đăng nhập khỏi SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.bottomFragment, fragment).commit()
    }

}
