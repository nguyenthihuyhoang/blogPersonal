package com.example.personal

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import com.example.personal.Data.AdminData
import com.example.personal.Data.PostData
import com.example.personal.Data.favoriteData
import com.example.personal.databinding.FragmentFavoriteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FavoriteFragment : Fragment() {

    private lateinit var binding: FragmentFavoriteBinding
    private lateinit var postList: ArrayList<PostData>
    private lateinit var favoriteList: ArrayList<favoriteData>
    private lateinit var adminList: ArrayList<AdminData>
    private lateinit var adapter: MyAdapter
    private var databaseReference: DatabaseReference? = null
    private var eventListener: ValueEventListener? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private var username: String? = null
    private var useremail: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_home, container, false)
        binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser
        val userId = user?.uid

        if (userId == null) {
            val intent = Intent(activity, LoginActivity::class.java).apply {
                putExtra("messenger", "You have not LOGIN")
            }
            startActivity(intent)
        }

        // Nhận username từ arguments nếu có
//        username = activity?.intent?.getStringExtra("username")
//        useremail = activity?.intent?.getStringExtra("useremail")
        // Set up Toolbar
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)

        // Set up RecyclerView
        val gridLayoutManager = GridLayoutManager(requireContext(), 1)
        binding.postsRecyclerView.layoutManager = gridLayoutManager

        // Hiển thị ProgressDialog
        val builder = AlertDialog.Builder(requireContext())
        builder.setCancelable(false)
        builder.setView(R.layout.layout_progress)
        val dialog = builder.create()
        dialog.show()

        // Khởi tạo danh sách bài viết và adapter
        favoriteList = ArrayList()
        postList = ArrayList()
        adminList = ArrayList()
        val currentDate = activity?.intent?.getStringExtra("currentDate") ?: ""
        adapter = MyAdapter(requireContext(), postList,  favoriteList, adminList, currentDate, showButtons = false)
        binding.postsRecyclerView.adapter = adapter

        // Lấy dữ liệu từ Firebase
//        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
//        val userId = sharedPref.getString("userId", null)
            databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId!!).child("favorites")
            eventListener = databaseReference!!.orderByChild("status").equalTo(true).addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    favoriteList.clear()
                    for (itemSnapshot in snapshot.children) {
                        val favoriteData = itemSnapshot.getValue(favoriteData::class.java)
                        Log.d("favorite", "Retrieved item: ${itemSnapshot.value}")
                        favoriteList.add(favoriteData!!)
                        Log.d("favorite", "Added to list: $favoriteData")
                    }
                    adapter.notifyDataSetChanged()
                    dialog.dismiss()
                    Log.d("favorite", "Total items in list: ${favoriteList.size}")
                }

                override fun onCancelled(error: DatabaseError) {
                    dialog.dismiss()
                }
            })
    }

}