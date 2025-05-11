package com.example.shopthucungAdmin_master.user.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopthucungAdmin_master.model.Order
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OrderViewModel(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) : ViewModel() {
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders

    fun fetchOrders() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("order").get().await()
                val orderList = snapshot.documents.mapNotNull { it.toObject(Order::class.java) }
                _orders.value = orderList
            } catch (e: Exception) {
                println("Error loading orders: ${e.message}")
            }
        }
    }
}
