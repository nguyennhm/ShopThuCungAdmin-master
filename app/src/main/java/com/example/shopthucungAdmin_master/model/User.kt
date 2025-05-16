package com.example.shopthucungAdmin_master.model

import androidx.annotation.Keep
import com.google.firebase.firestore.PropertyName

@Keep
data class User(
    @PropertyName("diaChi") val diaChi: String = "",
    @PropertyName("sdt") val sdt: String = "",
    @PropertyName("email") val email: String = "",
    @PropertyName("hoVaTen") val hoVaTen: String = "",
    @PropertyName("idUser") val idUser: String = "",
    @PropertyName("role") val role: String = "",
    @PropertyName("active") val active: Boolean = true,
    @PropertyName("matKhau") val matKhau: String = ""
)