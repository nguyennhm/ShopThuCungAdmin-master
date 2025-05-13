package com.example.shopthucungAdmin_master.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.Timestamp
data class Order(
    @PropertyName("orderId") val orderId: String = "",
    @PropertyName("userId") val userId: String = "",
    @PropertyName("productId") val productId: Int = 0,
    @PropertyName("product") val product: Product? = null,
    @PropertyName("quantity") val quantity: Int = 0,
    @PropertyName("totalPrice") val totalPrice: Long = 0,
    @PropertyName("paymentMethod") val paymentMethod: String = "",
    @PropertyName("status") val status: String = "Đang xử lý",
    @PropertyName("bookingDate") val bookingdate: Timestamp? = Timestamp.now(),
    @PropertyName("deliveryDate") val deliverydate: Timestamp? = null

)