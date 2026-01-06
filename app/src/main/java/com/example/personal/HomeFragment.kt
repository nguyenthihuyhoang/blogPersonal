package com.example.personal

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import com.example.personal.Data.AdminData
import com.example.personal.Data.PostData
import com.example.personal.Data.favoriteData
import com.example.personal.databinding.FragmentHomeBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
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
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_home, container, false)
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Nhận username từ arguments nếu có
        username = activity?.intent?.getStringExtra("username")

        // Set up Toolbar
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "Home"

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
        adapter = MyAdapter(requireContext(), postList, favoriteList, adminList, currentDate, showButtons = false)
        binding.postsRecyclerView.adapter = adapter

        // Lấy dữ liệu từ Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("Posts")
        eventListener = databaseReference!!.orderByChild("status").equalTo(true)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    postList.clear()
                    for (itemSnapshot in snapshot.children) {
                        val postData = itemSnapshot.getValue(PostData::class.java)
                        postData?.let {
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

        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                //pending
                filter(newText)
                return false
            }

        })

    }

    private fun filter(text: String?) {
        val filteredPostList = if (text.isNullOrEmpty()) {
            postList
        } else {
            postList.filter {
                it.topic!!.contains(text, true) || it.desc!!.contains(text, true) || it.username!!.contains(text, true)
            }
        }

        val filteredFavoriteList = if (text.isNullOrEmpty()) {
            favoriteList
        } else {
            favoriteList.filter {
                it.topic!!.contains(text, true) || it.desc!!.contains(text, true) || it.username!!.contains(text, true)
            }
        }

//        val filteredAdminList = if (text.isNullOrEmpty()) {
//            adminList
//        } else {
//            adminList.filter {
//                it.topic!!.contains(text, true) || it.desc!!.contains(text, true) || it.username!!.contains(text, true)
//            }
//        }

        adapter.searchData(filteredPostList, filteredFavoriteList)
    }


}