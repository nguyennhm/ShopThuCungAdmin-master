package com.example.shopthucungAdmin_master.model

import com.google.firebase.firestore.PropertyName

data class Product(
    @PropertyName("anh_sp") var anh: List<String> = emptyList(),  // Ánh xạ trường anh_sp từ Firestore
    @PropertyName("ten_sp") var ten_sp: String = "",
    @PropertyName("mota") var mo_ta: String = "",
    @PropertyName("gia_sp") var gia_sp: Long = 0L,
    @PropertyName("soluong") var soluong: Int = 0,
    @PropertyName("giam_gia") var giam_gia: Int = 0,
    @PropertyName("id_sanpham") var id_sanpham: Int = 0,
    @PropertyName("so_luong_ban") var so_luong_ban: Int = 0,
    @PropertyName("danh_gia") var danh_gia: Double = 0.0,  // Thêm trường danh_gia
    var firestoreId: String = ""  // Lưu firestoreId
)
