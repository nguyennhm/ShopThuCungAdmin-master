package com.example.shopthucungAdmin_master.admin.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.shopthucungAdmin_master.model.Banner
import com.google.firebase.firestore.FirebaseFirestore

class BannerViewModel : ViewModel() {
    var banners by mutableStateOf(listOf<Banner>())
        private set

    private val firestore = FirebaseFirestore.getInstance()

    init {
        fetchBannersFromFirestore()
    }

    // Lấy danh sách banner từ Firestore khi khởi tạo
    private fun fetchBannersFromFirestore() {
        firestore.collection("banner")
            .get()
            .addOnSuccessListener { documents ->
                val bannerList = documents.mapNotNull { doc ->
                    doc.toObject(Banner::class.java).copy(id_banner = doc.id.replace("banner_", "").toInt())
                }
                banners = bannerList.sortedBy { it.id_banner } // Sắp xếp theo id_banner
            }
            .addOnFailureListener { exception ->
                println("❌ Lỗi khi tải danh sách banner: ${exception.message}")
            }
    }

    // Cập nhật trạng thái
    fun updateStatus(id: Int, newStatus: String) {
        banners = banners.map {
            if (it.id_banner == id) it.copy(status = newStatus) else it
        }
        // Cập nhật trên Firestore
        firestore.collection("banner").document("$id")
            .update("status", newStatus)
            .addOnFailureListener { exception ->
                println("❌ Cập nhật trạng thái thất bại: ${exception.message}")
            }
    }

    // Xóa banner
    fun deleteBanner(id: Int) {
        banners = banners.filterNot { it.id_banner == id }
        // Xóa khỏi Firestore
        firestore.collection("banner").document("$id")
            .delete()
            .addOnFailureListener { exception ->
                println("❌ Xóa banner thất bại: ${exception.message}")
            }
    }

    // Thêm banner với id tự động tăng từ 1
    fun addBanner(banner: Banner) {
        val newId = if (banners.isEmpty()) 1 else banners.maxOf { it.id_banner } + 1
        val newBanner = banner.copy(id_banner = newId)
        banners = banners + newBanner
    }

    // Lưu banner vào Firestore với tên document dựa trên id_banner
    fun saveBannerToFirestore(banner: Banner) {
        val newId = if (banners.isEmpty()) 1 else banners.maxOf { it.id_banner } + 1
        val newBanner = banner.copy(id_banner = newId)
        firestore.collection("banner").document("$newId")
            .set(newBanner)
            .addOnSuccessListener {
                addBanner(newBanner) // Cập nhật danh sách local
            }
            .addOnFailureListener { exception ->
                println("❌ Lưu banner thất bại: ${exception.message}")
            }
    }
}