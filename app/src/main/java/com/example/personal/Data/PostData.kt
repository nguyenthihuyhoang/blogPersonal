package com.example.personal.Data

data class PostData (
    var username: String? = null,
    var image: String? = null,
    var topic: String? = null,
    var desc: String? = null,
    var postid: String? = null,
    var currentDate: String? = null,
    var status: Boolean = true
)