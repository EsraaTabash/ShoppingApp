package com.example.android2finalproject.models

data class Order(
    var orderId: String? = null,
    var userId: String? = null,
    var total: Double? = null,
    var count: Int? = null,
    var createdAt: Long? = null
)
