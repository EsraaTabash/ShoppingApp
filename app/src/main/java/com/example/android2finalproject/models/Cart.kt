package com.example.android2finalproject.models

data class CartItem(
    val productId: String = "",
    val pName: String = "",
    val pDescription: String = "",
    val imageUri: String = "",
    val pPrice: Double = 0.0,
    val qty: Long = 1,
    val categoryId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
