package com.example.android2finalproject.models

data class Rating(
    var userId: String? = null,
    var productId: String? = null,
    var rating: Double? = null,
    var comment: String? = null,
    var createdAt: Long? = null
)
