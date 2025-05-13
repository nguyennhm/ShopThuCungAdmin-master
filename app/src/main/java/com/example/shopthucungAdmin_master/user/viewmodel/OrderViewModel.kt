package com.example.shopthucungAdmin_master.user.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopthucungAdmin_master.model.Order
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class OrderViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _orderList = MutableStateFlow<List<Order>>(emptyList())
    val orderList: StateFlow<List<Order>> = _orderList

    private val _filteredOrders = MutableStateFlow<List<Order>>(emptyList())
    val filteredOrders: StateFlow<List<Order>> = _filteredOrders

    private var currentProductNameFilter: String = ""
    private var currentStatusFilter: String? = null
    private var currentDateFilter: Date? = null
    private var currentFromDateFilter: Date? = null
    private var currentToDateFilter: Date? = null

    fun fetchOrders() {
        viewModelScope.launch {
            db.collection("orders")
                .get()
                .addOnSuccessListener { result ->
                    val orders = result.documents.mapNotNull { it.toObject(Order::class.java) }
                    _orderList.value = orders
                    applyFilters(
                        currentProductNameFilter,
                        currentStatusFilter,
                        currentDateFilter,
                        currentFromDateFilter?.let { sdf.format(it) } ?: "",
                        currentToDateFilter?.let { sdf.format(it) } ?: ""
                    )
                }
                .addOnFailureListener { exception ->
                    println("Lỗi khi fetch orders: ${exception.message}")
                }
        }
    }

    fun applyFilters(
        productName: String,
        status: String?,
        date: Date?,
        fromDateStr: String = "",
        toDateStr: String = ""
    ) {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.isLenient = false // Đảm bảo kiểm tra định dạng nghiêm ngặt

        // Chuyển đổi chuỗi ngày thành Date, nếu không hợp lệ thì để null
        val fromDate = try {
            if (fromDateStr.isNotBlank()) sdf.parse(fromDateStr) else null
        } catch (e: Exception) {
            null // Lỗi định dạng đã được xử lý trong OrderListScreen
        }

        val toDate = try {
            if (toDateStr.isNotBlank()) sdf.parse(toDateStr) else null
        } catch (e: Exception) {
            null // Lỗi định dạng đã được xử lý trong OrderListScreen
        }

        // Cập nhật các biến trạng thái
        currentProductNameFilter = productName
        currentStatusFilter = status
        currentDateFilter = date
        currentFromDateFilter = fromDate
        currentToDateFilter = toDate

        // Lọc danh sách
        val filtered = _orderList.value.filter { order ->
            val matchProduct = productName.isBlank() || (order.product?.ten_sp?.contains(productName, ignoreCase = true) == true)
            val matchStatus = status == null || order.status == status
            val matchDate = date == null || isSameDay(order.timestamp.toDate(), date)
            val matchFromDate = fromDate == null || !order.timestamp.toDate().before(fromDate)
            val matchToDate = toDate == null || !order.timestamp.toDate().after(toDate)

            matchProduct && matchStatus && matchDate && matchFromDate && matchToDate
        }
        _filteredOrders.value = filtered
    }

    fun updateOrderStatus(orderId: String, newStatus: String, productName: String, status: String?, date: Date?) {
        viewModelScope.launch {
            val docRef = db.collection("orders").whereEqualTo("orderId", orderId)
            docRef.get().addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val docId = snapshot.documents.first().id
                    db.collection("orders").document(docId)
                        .update("status", newStatus)
                        .addOnSuccessListener {
                            // Cập nhật danh sách cục bộ
                            _orderList.value = _orderList.value.map { order ->
                                if (order.orderId == orderId) order.copy(status = newStatus) else order
                            }
                            // Áp dụng lại bộ lọc với tham số hiện tại
                            applyFilters(
                                productName,
                                status,
                                date,
                                currentFromDateFilter?.let { sdf.format(it) } ?: "",
                                currentToDateFilter?.let { sdf.format(it) } ?: ""
                            )
                        }
                        .addOnFailureListener { exception ->
                            println("Lỗi khi cập nhật trạng thái: ${exception.message}")
                        }
                } else {
                    println("Không tìm thấy đơn hàng với orderId: $orderId")
                }
            }
        }
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    companion object {
        private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    }
}