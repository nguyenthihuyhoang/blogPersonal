package com.example.personal

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.personal.Data.PostData
import com.example.personal.Data.favoriteData
import com.example.personal.databinding.ActivityDetailpostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DetailpostActivity : AppCompatActivity() {

    var imageURL = ""
    private lateinit var binding: ActivityDetailpostBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private var isFavorite = false
    private var postid: String? = null
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailpostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users")
        firebaseAuth = FirebaseAuth.getInstance()

        userId = firebaseAuth.currentUser?.uid
        postid = intent.getStringExtra("postid")

        binding.detailTopic.text = intent.getStringExtra("topic")
        imageURL = intent.getStringExtra("image")!!
        Glide.with(this).load(imageURL).into(binding.detailImage)
        binding.detailDesc.text = intent.getStringExtra("desc")

        val detailDesc = findViewById<TextView>(R.id.detailDesc)
        detailDesc.movementMethod = ScrollingMovementMethod()

        checkFavoriteStatus()

        if (userId == null) {
            val intent = Intent(this, LoginActivity::class.java).apply {
                putExtra("messenger", "You have not LOGIN")
            }
            startActivity(intent)
        } else {
            binding.favorite.setOnClickListener {
                toggleFavorite()
        }

        }
    }

    private fun checkFavoriteStatus() {
        //đã login
        if (userId != null && postid != null) {
            databaseReference.child(userId!!).child("favorites").child(postid!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        isFavorite = snapshot.exists()
                        updateFavoriteButton(isFavorite)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@DetailpostActivity, "Failed to load favorite status", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun toggleFavorite() {
//        val username = intent.getStringExtra("username")
//        if (username == null) {
//            val intent = Intent(this, LoginActivity::class.java).apply {
//                putExtra("messenger", "You have not LOGIN")
//            }
//            startActivity(intent)
//            finish()
//        } else
            if (userId != null && postid != null) {
            val favoriteRef = databaseReference.child(userId!!).child("favorites").child(postid!!)

            if (isFavorite) {
                // Remove from favorites
                favoriteRef.removeValue().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        isFavorite = false
                        updateFavoriteButton(false)
                        Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to remove from favorites", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Add to favorites
                val topic = intent.getStringExtra("topic")
                val desc = intent.getStringExtra("desc")
                val currentDate = intent.getStringExtra("currentDate")
                val username = intent.getStringExtra("username")
                val image = intent.getStringExtra("image")

                val favoriteData = favoriteData(
                    postid = postid!!,
                    currentDate = currentDate,
                    username = username,
                    image = image,
                    topic = topic,
                    desc = desc,
                )

                favoriteRef.setValue(favoriteData).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        isFavorite = true
                        updateFavoriteButton(true)
                        Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to add to favorites", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateFavoriteButton(isFavorite: Boolean) {
        if (isFavorite) {
            binding.favorite.setImageResource(R.drawable.baseline_favorite_24)
        } else {
            binding.favorite.setImageResource(R.drawable.baseline_favorite_border_24)
        }
    }
}
