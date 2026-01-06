package com.example.personal

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.personal.databinding.ActivityUpdatepostBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class UpdatepostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdatepostBinding
    private lateinit var databaseReference: DatabaseReference
    var image: String? = null
    var uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdatepostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //nút quay lại
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        val username = intent.getStringExtra("username")
        val topic = intent.getStringExtra("topic")
        val desc = intent.getStringExtra("desc")
        val image = intent.getStringExtra("image")

        binding.updateTopic.setText(topic)
        binding.updateDesc.setText(desc)
        Glide.with(this).load(image).into(binding.updateImage)

        val activityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                uri = data!!.data
                binding.updateImage.setImageURI(uri)
            } else {
                Toast.makeText(this@UpdatepostActivity, "No Image Selected", Toast.LENGTH_SHORT).show()
            }
        }

        binding.updateImage.setOnClickListener {
            val photoPicker = Intent(Intent.ACTION_PICK)
            photoPicker.type = "image/*"
            activityResultLauncher.launch(photoPicker)
        }

        binding.buttonUpdate.setOnClickListener {
            updateImage()
            val username = intent.getStringExtra("username")
            val intent = Intent(this@UpdatepostActivity, MainActivity2::class.java)
            intent.putExtra("ProfileFragment", "ProfileFragment")
            intent.putExtra("username", username)
            startActivity(intent)
            finish()
        }
    }


    private fun updateImage() {
        val image = intent.getStringExtra("image")
        val topic = binding.updateTopic.text.toString()
        val desc = binding.updateDesc.text.toString()
        val dialog = createProgressDialog()

        if (uri != null) {
            // Người dùng chọn hình ảnh mới, cập nhật hình ảnh và thông tin
            val storageReference = FirebaseStorage.getInstance().reference.child("Posts/${uri!!.lastPathSegment}")
            storageReference.putFile(uri!!)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { urlImage ->
                            updateData(urlImage.toString(), topic, desc, dialog)
                    }
                }
                .addOnFailureListener {
                    dialog.dismiss()
                    Toast.makeText(this, "Failed to upload new image", Toast.LENGTH_SHORT).show()
                }
        } else {

            // Không có hình ảnh mới, chỉ cập nhật thông tin
                updateData(image!!, topic, desc, dialog)
        }
    }

    private fun updateData(image: String, topic: String, desc: String, dialog: AlertDialog) {
        val postid = intent.getStringExtra("postid")
        val updateMap = mapOf<String, Any>(
            "image" to image,
            "topic" to topic,
            "desc" to desc
        )
        Log.d("post id", postid!!)
        val currentDate = intent.getStringExtra("currentDate")
        val postRef = FirebaseDatabase.getInstance().getReference("Posts").child(postid)
        postRef.updateChildren(updateMap).addOnCompleteListener { task ->
            dialog.dismiss()
            if (task.isSuccessful) {
                Toast.makeText(this, "Post Updated", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to update post", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
                Toast.makeText(this@UpdatepostActivity, e.message.toString(), Toast.LENGTH_SHORT).show() }
    }




    private fun createProgressDialog(): AlertDialog {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setView(R.layout.layout_progress)
        val dialog = builder.create()
        dialog.show()
        return dialog
    }


}