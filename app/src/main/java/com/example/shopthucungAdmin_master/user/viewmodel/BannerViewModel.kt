package com.example.shopthucungAdmin_master.user.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.shopthucungAdmin_master.model.Banner

class BannerViewModel : ViewModel() {
    var banners by mutableStateOf(listOf<Banner>())
        private set

    fun updateStatus(id: Int, newStatus: String) {
        banners = banners.map {
            if (it.id_banner == id) it.copy(status = newStatus) else it
        }
    }

    fun deleteBanner(id: Int) {
        banners = banners.filterNot { it.id_banner == id }
    }

    fun addBanner(banner: Banner) {
        banners = banners + banner
    }
}

