package com.example.android2finalproject.models

data class User(
    var id: String? = null,
    var name: String? = null,
    var email: String? = null,
    var isAdmin: Boolean = false,
    var phone: String? = null,
    var address: String? = null
)
