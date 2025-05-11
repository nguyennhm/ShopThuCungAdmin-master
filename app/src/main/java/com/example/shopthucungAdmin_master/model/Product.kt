package com.example.shopthucungAdmin_master.model

data class Product(
    var anh_sp: List<String> = emptyList(),  // ✅ Cho phép lưu nhiều ảnh
    val danh_gia: Float = 0f,
    val gia_sp: Long = 0L,
    var id_sanpham: Int = 0,
    val mo_ta: String = "",
    var so_luong_ban: Int = 0,
    var soluong: Int = 0,
    val ten_sp: String = "",
    val giam_gia: Int = 5,
    )
