package com.example.android2finalproject.models

data class OrderItem(
    var productId: String? = null,
    var pName: String? = null,
    var pPrice: Double? = null,
    var imageUri: String? = null,
    var quantity: Int? = null
)
