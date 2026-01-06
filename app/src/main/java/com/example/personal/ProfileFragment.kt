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
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.personal.Data.AdminData
import com.example.personal.Data.PostData
import com.example.personal.Data.favoriteData
import com.example.personal.databinding.FragmentProfileBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var postList: ArrayList<PostData>
    private lateinit var favoriteList: ArrayList<favoriteData>
    private lateinit var adminList: ArrayList<AdminData>
    private lateinit var adapter: MyAdapter
    private var databaseReference: DatabaseReference? = null
    private var eventListener: ValueEventListener? = null
    private var username: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Nhận username từ arguments nếu có
//        username = activity?.intent?.getStringExtra("username")
        username = activity?.intent?.getStringExtra("username")

        binding.username.text = username

//        //kiem tra email từ login xem đã login hay chưa
//        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
//        val email = sharedPreferences.getString("email", null)
//        if (email == null) {
//            val intent = Intent(activity, LoginActivity::class.java).apply {
//                putExtra("messenger", "You have not LOGIN")
//            }
//            startActivity(intent)
//        }

        // Set up Toolbar
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "Profile"

        //Xử lý đăng xuất
        val logout = view.findViewById<ImageView>(R.id.logout)
        logout.setOnClickListener {
            //quay lại login
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

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
        adapter = MyAdapter(requireContext(), postList, favoriteList, adminList, currentDate, showButtons = true)
        binding.postsRecyclerView.adapter = adapter

        // Lấy dữ liệu từ Firebase
        val query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("username").equalTo(username)
        eventListener = query.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                postList.clear()
                for (itemSnapshot in snapshot.children) {
                    val postData = itemSnapshot.getValue(PostData::class.java)
                    if (postData != null) {
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
