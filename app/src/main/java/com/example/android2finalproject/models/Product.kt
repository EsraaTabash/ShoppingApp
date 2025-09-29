package com.example.android2finalproject.models

data class Product(
    var pName: String? = null,
    var pDescription: String? = null,
    var pPrice: Double? = null,
    var categoryId: String? = null,
    var pRating: Double? = null,
    var imageUri: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null
)
