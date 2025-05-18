package com.example.shopthucungAdmin_master.model

import com.google.firebase.Timestamp

data class Notification(
    val idNotification: Int,
    val orderId: String = "",
    val content: String = "",
    val Notdate: Timestamp? = Timestamp.now(),
    val idUser: String = "",
    val isReaded: Boolean = false
)