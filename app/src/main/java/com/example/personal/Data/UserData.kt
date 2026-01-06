package com.example.personal.Data

data class UserData(
    val id: String? = null,
    val username: String? = null,
    val password: String? = null,
    val gender: String? = null,
    val email: String? = null,
    val role: String = "user",
    var favorites: Map<String, favoriteData>? = null
)
