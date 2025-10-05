package com.example.android2finalproject.models

data class Product(
    val pName: String = "",
    val pDescription: String = "",
    val pPrice: Double = 0.0,
    val pType: String = "",
    val pRating: Double = 0.0,
    val imageUri: String = "",
    val id: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
)
