package com.example.personal

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.example.personal.Data.PostData
import com.example.personal.databinding.ActivityAddpostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.text.DateFormat
import java.util.Calendar

class AddpostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddpostBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth

    var image: String? = null
    var uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddpostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //nút quay lại
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        val username = intent.getStringExtra("username")
        if (username != null) {
            Log.d("namepost", username)
        }

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("Posts")
        firebaseAuth = FirebaseAuth.getInstance()

        val activityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                uri = data!!.data
                binding.addImage.setImageURI(uri)
            } else {
                Toast.makeText(this@AddpostActivity, "No Image Selected", Toast.LENGTH_SHORT).show()
            }
        }
        binding.addImage.setOnClickListener {
            val photoPicker = Intent(Intent.ACTION_PICK)
            photoPicker.type = "image/*"
            activityResultLauncher.launch(photoPicker)
        }
        binding.buttonSaveAdd.setOnClickListener {
            addImage()
        }
    }


    private fun addImage() {
        val desc = binding.addDesc.text.toString()
        val storageReference = FirebaseStorage.getInstance().reference.child("Posts")
            .child(uri!!.lastPathSegment!!)

        val builder = AlertDialog.Builder(this@AddpostActivity)
        builder.setCancelable(false)
        builder.setView(R.layout.layout_progress)
        val dialog = builder.create()
        dialog.show()

        storageReference.putFile(uri!!).addOnSuccessListener { taskSnapshot ->
            val uriTask = taskSnapshot.storage.downloadUrl
            while (!uriTask.isComplete);
            val urlImage = uriTask.result
            image = urlImage.toString()
            addData(desc)
            dialog.dismiss()
        }.addOnFailureListener{
            dialog.dismiss()
        }
    }

    private fun addData(desc: String) {
        databaseReference.orderByChild("desc").equalTo(desc).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(datasnapshot: DataSnapshot) {
                if (!datasnapshot.exists()) {
                    val topic = binding.addTopic.text.toString()
                    val desc = binding.addDesc.text.toString()
                    val username = intent.getStringExtra("username")
                    if (username != null) {
                        Log.d("nameinpost: ", username)
                    }
                    val postid = databaseReference.push().key
                    val currentDate = DateFormat.getDateTimeInstance().format(Calendar.getInstance().time)
                    val postData = PostData(
                        postid = postid,
                        currentDate = currentDate,
                        username = username,
                        image = image,
                        topic = topic,
                        desc = desc,


                    )
                    firebaseDatabase.getReference("Posts").child(postid!!).setValue(postData)
                    Toast.makeText(this@AddpostActivity, "Saved", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@AddpostActivity, MainActivity2::class.java)
                    intent.putExtra("ProfileFragment", "ProfileFragment")
                    intent.putExtra("username", username)
                    intent.putExtra("currentDate", currentDate)
                    startActivity(intent)
//                    startActivity(Intent(this@Addpost, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@AddpostActivity, "Content already exists", Toast.LENGTH_SHORT).show()
                }

            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@AddpostActivity, "Database Error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
//        val topic = binding.addTopic.text.toString()
//        val desc = binding.addDesc.text.toString()
//
//        val currentDate = DateFormat.getDateTimeInstance().format(Calendar.getInstance().time)
//        val postData = PostData(image, topic, desc, postid)
//
//        FirebaseDatabase.getInstance().getReference("Posts").child(currentDate)
//            .setValue(postData).addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    Toast.makeText(this@Addpost, "Saved", Toast.LENGTH_SHORT).show()
//                    startActivity(Intent(this@Addpost, MainActivity::class.java))
//                    finish()
//                }
//
//            }.addOnFailureListener { e ->
//                Toast.makeText(this@Addpost, e.message.toString(), Toast.LENGTH_SHORT).show()
//            }
    }
}