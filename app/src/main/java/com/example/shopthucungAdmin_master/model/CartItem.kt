package com.example.shopthucungAdmin_master.model

data class CartItem(
    val userId: String = "",
    val productId: Int = 0,
    val quantity: Int = 0,
    val product: Product? = null,
    val cartIndex: Int = 0
)
