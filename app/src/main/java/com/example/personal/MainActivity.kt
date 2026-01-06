package com.example.personal

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import com.example.personal.Data.AdminData
import com.example.personal.Data.PostData
import com.example.personal.Data.favoriteData
import com.example.personal.databinding.ActivityMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var postList: ArrayList<PostData>
    private lateinit var favoriteList: ArrayList<favoriteData>
    private lateinit var adminList: ArrayList<AdminData>
    private lateinit var adapter: MyAdapter
    var databaseReference: DatabaseReference? = null
    var eventListener: ValueEventListener? = null
    private var username: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        username = intent.getStringExtra("username")
        binding.addBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, AddpostActivity::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
            finish()
        }


        val gridLayoutManager = GridLayoutManager(this@MainActivity, 1)
        binding.postsRecyclerView.layoutManager = gridLayoutManager

        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setCancelable(false)
        builder.setView(R.layout.layout_progress)
        val dialog = builder.create()
        dialog.show()
        favoriteList = ArrayList()
        postList = ArrayList()
        adminList = ArrayList()
        val currentDate = intent.getStringExtra("currentDate")?: ""

        adapter = MyAdapter(this@MainActivity, postList, favoriteList, adminList, currentDate, showButtons = false)
        binding.postsRecyclerView.adapter = adapter
        databaseReference = FirebaseDatabase.getInstance().getReference("Posts")
        dialog.show()

        eventListener = databaseReference!!.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                postList.clear()
                for(itemSnapshot in snapshot.children) {
                    val postData = itemSnapshot.getValue(PostData::class.java)
                    if(postData != null) {
                        postList.add(postData)
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