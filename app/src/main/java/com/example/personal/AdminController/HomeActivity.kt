package com.example.personal.AdminController

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import com.example.personal.Data.AdminData
import com.example.personal.Data.PostData
import com.example.personal.Data.favoriteData
import com.example.personal.LoginActivity
import com.example.personal.MyAdapter
import com.example.personal.R
import com.example.personal.databinding.AdminActivityHomeBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: AdminActivityHomeBinding
    private lateinit var postList: ArrayList<PostData>
    private lateinit var favoriteList: ArrayList<favoriteData>
    private lateinit var adminList: ArrayList<AdminData>
    private lateinit var adapter: MyAdapter
    var databaseReference: DatabaseReference? = null
    var eventListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = AdminActivityHomeBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "Home"

        //logout
        val logout = findViewById<ImageView>(R.id.logout)
        logout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        val gridLayoutManager = GridLayoutManager(this@HomeActivity, 1)
        binding.postsRecyclerContent.layoutManager = gridLayoutManager

        val builder = AlertDialog.Builder(this@HomeActivity)
        builder.setCancelable(false)
        builder.setView(R.layout.layout_progress)
        val dialog = builder.create()
        dialog.show()
        favoriteList = ArrayList()
        postList = ArrayList()
        adminList = ArrayList()
        val currentDate = intent.getStringExtra("currentDate")?: ""

        adapter = MyAdapter(this@HomeActivity, postList, favoriteList, adminList, currentDate, showButtons = false)
        binding.postsRecyclerContent.adapter = adapter
        databaseReference = FirebaseDatabase.getInstance().getReference("Posts")
        dialog.show()

        eventListener = databaseReference!!.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                adminList.clear()
                for(itemSnapshot in snapshot.children) {
                    val adminData = itemSnapshot.getValue(AdminData::class.java)
                    if(adminData != null) {
                        adminList.add(adminData)
                    }
                }
                adapter.notifyDataSetChanged()
                dialog.dismiss()
            }

            override fun onCancelled(error: DatabaseError) {
                dialog.dismiss()
            }
        })
    }
}