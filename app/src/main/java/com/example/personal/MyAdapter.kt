package com.example.personal

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.personal.Data.AdminData
import com.example.personal.Data.PostData
import com.example.personal.Data.UserData
import com.example.personal.Data.favoriteData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MyAdapter(
    private val context:Context,
    private var postList:List<PostData>,
    private var favoriteList: List<favoriteData>,
    private var adminList: List<AdminData>,
    private val currentDate: String,
    private val showButtons: Boolean
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var eventListener: ValueEventListener? = null


    companion object {
        private const val TYPE_POST = 0
        private const val TYPE_FAVORITE = 1
        private const val TYPE_ADMIN = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position < postList.size -> TYPE_POST
            position < postList.size + favoriteList.size -> TYPE_FAVORITE
            else -> TYPE_ADMIN
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            TYPE_POST -> {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recycler_item, parent, false)
                PostViewHolder(view)
            }

            TYPE_FAVORITE -> {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.favorite_item, parent, false)
                FavoriteViewHolder(view)
            }

            TYPE_ADMIN -> {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.admin_post_item, parent, false)
                AdminViewHolder(view)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_POST) {
            val postViewHolder = holder as PostViewHolder
            val post = postList[position]
            Glide.with(context).load(post.image).into(postViewHolder.reImage)
            postViewHolder.showTopic.text = post.topic
            postViewHolder.showDesc.text = post.desc
            postViewHolder.username.text = post.username
            postViewHolder.currentDate.text = post.currentDate

            postViewHolder.reCard.setOnClickListener {
                val intent = Intent(context, DetailpostActivity::class.java)
                intent.putExtra("postid", post.postid)
                intent.putExtra("currentDate", post.currentDate)
                intent.putExtra("username", post.username)
                intent.putExtra("image", post.image)
                intent.putExtra("topic", post.topic)
                intent.putExtra("desc", post.desc)
                context.startActivity(intent)
            }

            if (showButtons) {
                postViewHolder.updateBtn.visibility = View.VISIBLE
                postViewHolder.deleteBtn.visibility = View.VISIBLE

                postViewHolder.updateBtn.setOnClickListener {
                    val intent = Intent(context, UpdatepostActivity::class.java)
                    intent.putExtra("username", post.username)
                    intent.putExtra("postid", post.postid)
                    intent.putExtra("image", post.image)
                    intent.putExtra("topic", post.topic)
                    intent.putExtra("desc", post.desc)
                    intent.putExtra("currentDate", currentDate)
                    Log.d("ngay", currentDate)
                    context.startActivity(intent)
                }

                postViewHolder.deleteBtn.setOnClickListener {
                    deletePost(post.postid!!)
                }
            } else {
                postViewHolder.updateBtn.visibility = View.GONE
                postViewHolder.deleteBtn.visibility = View.GONE
            }
        } else if(getItemViewType(position) == TYPE_FAVORITE) {
            val favoriteViewHolder = holder as FavoriteViewHolder
            val favorite = favoriteList[position - postList.size]
            Glide.with(context).load(favorite.image).into(favoriteViewHolder.fvImage)
            favoriteViewHolder.fvTopic.text = favorite.topic
            favoriteViewHolder.fvDesc.text = favorite.desc
            favoriteViewHolder.fvusername.text = favorite.username
            favoriteViewHolder.fvcurrentDate.text = favorite.currentDate

            favoriteViewHolder.fvCard.setOnClickListener {
                val intent = Intent(context, DetailpostActivity::class.java)
                intent.putExtra("postid", favorite.postid)
                intent.putExtra("currentDate", favorite.currentDate)
                intent.putExtra("username", favorite.username)
                intent.putExtra("image", favorite.image)
                intent.putExtra("topic", favorite.topic)
                intent.putExtra("desc", favorite.desc)
                context.startActivity(intent)
            }

            favoriteViewHolder.fvupdateBtn.visibility = View.GONE
            favoriteViewHolder.fvdeleteBtn.visibility = View.GONE

            favoriteViewHolder.fvupdateBtn.setOnClickListener {
                val intent = Intent(context, UpdatepostActivity::class.java)
                intent.putExtra("username", favorite.username)
                intent.putExtra("postid", favorite.postid)
                intent.putExtra("image", favorite.image)
                intent.putExtra("topic", favorite.topic)
                intent.putExtra("desc", favorite.desc)
                intent.putExtra("currentDate", currentDate)
                Log.d("ngay", currentDate)
                context.startActivity(intent)
            }

            favoriteViewHolder.fvdeleteBtn.setOnClickListener {
                deletePost(favorite.postid!!)

            }
        } else {
            val adminViewHolder = holder as AdminViewHolder
            val admin = adminList[position - postList.size - favoriteList.size]
            Glide.with(context).load(admin.image).into(adminViewHolder.adImage)
            adminViewHolder.adTopic.text = admin.topic
            adminViewHolder.adDesc.text = admin.desc
            adminViewHolder.adusername.text = admin.username
            adminViewHolder.adcurrentDate.text = admin.currentDate

            // Lấy trạng thái từ vị trí "Admin" trong Firebase
            val adminRef = FirebaseDatabase.getInstance().getReference("Admin").child(admin.postid!!)
            adminRef.child("status").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val status = snapshot.getValue(Boolean::class.java) ?: true
                    adminViewHolder.status = status
                    if (status) {
                        adminViewHolder.statusBtn.setImageResource(R.drawable.baseline_done_24)
                    } else {
                        adminViewHolder.statusBtn.setImageResource(R.drawable.baseline_close_24)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("LoadStatus", "Failed to load admin status: ${error.message}")
                }
            })


            adminViewHolder.adCard.setOnClickListener {
                val intent = Intent(context, DetailpostActivity::class.java)
                intent.putExtra("postid", admin.postid)
                intent.putExtra("currentDate", admin.currentDate)
                intent.putExtra("username", admin.username)
                intent.putExtra("image", admin.image)
                intent.putExtra("topic", admin.topic)
                intent.putExtra("desc", admin.desc)
                context.startActivity(intent)
            }

            adminViewHolder.statusBtn.setOnClickListener {
                toggleApproveState(adminViewHolder, admin)
            }

        }
    }

    private fun toggleApproveState(adminViewHolder: AdminViewHolder, admin: AdminData) {
        adminViewHolder.status = !adminViewHolder.status
        if (adminViewHolder.status) {
            adminViewHolder.statusBtn.setImageResource(R.drawable.baseline_done_24)
        } else {
            adminViewHolder.statusBtn.setImageResource(R.drawable.baseline_close_24)
        }

        // Cập nhật trạng thái trong danh sách Posts
        val postRef = FirebaseDatabase.getInstance().getReference("Posts").child(admin.postid!!)
        postRef.child("status").setValue(adminViewHolder.status)

        // Cập nhật trạng thái trong danh sách Admin
        val adminRef = FirebaseDatabase.getInstance().getReference("Admin").child(admin.postid!!)
        adminRef.child("status").setValue(adminViewHolder.status)

        // Cập nhật trạng thái trong danh sách Favorites
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val role = snapshot.child("role").getValue(String::class.java)
                        if (role == "user") {
                            // Người dùng có vai trò là "user", thực hiện các hành động tương ứng
                            val favoritesRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("favorites").child(admin.postid!!)
                            favoritesRef.child("isStatus").setValue(adminViewHolder.status)
                        } else {
                            // Người dùng không có vai trò là "user", có thể là "admin" hoặc vai trò khác
                            // Thực hiện các hành động khác nếu cần thiết
                        }
                    } else {
                        // Không tìm thấy thông tin về người dùng trong cơ sở dữ liệu
                        // Thực hiện các hành động phù hợp nếu cần thiết
                        Log.e("UserLookup", "User not found in database")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Xử lý khi có lỗi xảy ra trong quá trình đọc dữ liệu từ cơ sở dữ liệu
                    Log.e("DatabaseError", "Error reading user data: ${error.message}")
                }
            })
        } else {
            Log.e("UserLookup", "User ID is null")
        }


    }

    fun searchData(filteredPostList: List<PostData>, filteredFavoriteList: List<favoriteData>) {
        this.postList = filteredPostList
        this.favoriteList = filteredFavoriteList
        notifyDataSetChanged()
    }

        private fun deletePost(postid: String) {
            FirebaseDatabase.getInstance().getReference("Posts").child(postid).removeValue()
        }

        override fun getItemCount(): Int {
            return postList.size + favoriteList.size + adminList.size
        }

    }



    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var reImage: ImageView = itemView.findViewById(R.id.reImage)
        var showTopic: TextView = itemView.findViewById(R.id.showTopic)
        var showDesc: TextView = itemView.findViewById(R.id.showDesc)
        var reCard: CardView = itemView.findViewById(R.id.reCard)
        var updateBtn: ImageView = itemView.findViewById(R.id.updateBtn)
        var deleteBtn: ImageView = itemView.findViewById(R.id.deleteBtn)
        var username: TextView = itemView.findViewById(R.id.username)
        var currentDate: TextView = itemView.findViewById(R.id.currentDate)
    }

    class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var fvImage: ImageView = itemView.findViewById(R.id.fvImage)
        var fvCard: CardView = itemView.findViewById(R.id.fvCard)
        var fvTopic: TextView = itemView.findViewById(R.id.fvTopic)
        var fvDesc: TextView = itemView.findViewById(R.id.fvDesc)
        var fvusername: TextView = itemView.findViewById(R.id.fvusername)
        var fvcurrentDate: TextView = itemView.findViewById(R.id.fvcurrentDate)
        var fvupdateBtn: ImageView = itemView.findViewById(R.id.fvupdateBtn)
        var fvdeleteBtn: ImageView = itemView.findViewById(R.id.fvdeleteBtn)
    }

    class AdminViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var adImage: ImageView = itemView.findViewById(R.id.adImage)
        var adCard: CardView = itemView.findViewById(R.id.adCard)
        var adTopic: TextView = itemView.findViewById(R.id.adTopic)
        var adDesc: TextView = itemView.findViewById(R.id.adDesc)
        var adusername: TextView = itemView.findViewById(R.id.adusername)
        var adcurrentDate: TextView = itemView.findViewById(R.id.adcurrentDate)
        var statusBtn: ImageButton = itemView.findViewById(R.id.statusBtn)
        var status: Boolean = true
}
