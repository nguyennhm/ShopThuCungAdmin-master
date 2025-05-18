package com.example.shopthucungAdmin_master.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopthucungAdmin_master.model.Notification
import com.example.shopthucungAdmin_master.model.Order
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
    private var currentBookingDateFilter: Date? = null
    private var currentFromDateFilter: Date? = null
    private var currentToDateFilter: Date? = null

    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
        isLenient = false
    }

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
                        currentBookingDateFilter,
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
        val fromDate = try {
            if (fromDateStr.isNotBlank()) sdf.parse(fromDateStr) else null
        } catch (e: Exception) {
            null
        }

        val toDate = try {
            if (toDateStr.isNotBlank()) sdf.parse(toDateStr) else null
        } catch (e: Exception) {
            null
        }

        currentProductNameFilter = productName
        currentStatusFilter = status
        currentBookingDateFilter = date
        currentFromDateFilter = fromDate
        currentToDateFilter = toDate

        val filtered = _orderList.value.filter { order ->
            val matchProduct = productName.isBlank() || (order.product?.ten_sp?.contains(productName, ignoreCase = true) == true)
            val matchStatus = status == null || order.status == status
            val matchDate = date == null || (order.bookingdate?.toDate()?.let { isSameDay(it, date) } == true)
            val matchFromDate = fromDate == null || (order.bookingdate?.toDate()?.let { !it.before(fromDate) } == true)
            val matchToDate = toDate == null || (order.bookingdate?.toDate()?.let { !it.after(toDate) } == true)

            matchProduct && matchStatus && matchDate && matchFromDate && matchToDate
        }
        _filteredOrders.value = filtered
    }

    fun updateOrderStatus(
        orderId: String,
        newStatus: String,
        productName: String,
        status: String?,
        bookingDate: Date?
    ) {
        viewModelScope.launch {
            val docRef = db.collection("orders").whereEqualTo("orderId", orderId)
            docRef.get().addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val docId = snapshot.documents.first().id
                    val order = snapshot.documents.first().toObject(Order::class.java)

                    db.collection("orders").document(docId)
                        .update("status", newStatus)
                        .addOnSuccessListener {
                            _orderList.value = _orderList.value.map { orderItem ->
                                if (orderItem.orderId == orderId) orderItem.copy(status = newStatus) else orderItem
                            }
                            applyFilters(
                                productName,
                                status,
                                bookingDate,
                                currentFromDateFilter?.let { sdf.format(it) } ?: "",
                                currentToDateFilter?.let { sdf.format(it) } ?: ""
                            )

                            if (newStatus == "Đang giao hàng" && order != null) {
                                viewModelScope.launch {
                                    try {
                                        val notificationsRef = db.collection("notifications")
                                        val snapshotNoti = notificationsRef.get().await()
                                        val maxId = snapshotNoti.documents.mapNotNull {
                                            it.getLong("idNotification")?.toInt()
                                        }.maxOrNull() ?: 0
                                        val nextId = maxId + 1

                                        val notification = Notification(
                                            idNotification = nextId,
                                            orderId = orderId,
                                            content = "Đơn hàng '${order.product?.ten_sp ?: ""}' đang trên đường giao",
                                            Notdate = Timestamp.now(),
                                            idUser = order.userId
                                        )

                                        notificationsRef.document(nextId.toString()).set(notification)
                                            .addOnSuccessListener {
                                                println("✅ Đã tạo thông báo với id $nextId")
                                            }
                                            .addOnFailureListener {
                                                println("❌ Lỗi tạo thông báo: ${it.message}")
                                            }
                                    } catch (e: Exception) {
                                        println("❌ Lỗi tạo thông báo: ${e.message}")
                                    }
                                }
                            } else if (newStatus == "Đã xác nhận" && order != null) {
                                viewModelScope.launch {
                                    try {
                                        val notificationsRef = db.collection("notifications")
                                        val snapshotNoti = notificationsRef.get().await()
                                        val maxId = snapshotNoti.documents.mapNotNull {
                                            it.getLong("idNotification")?.toInt()
                                        }.maxOrNull() ?: 0
                                        val nextId = maxId + 1

                                        val notification = Notification(
                                            idNotification = nextId,
                                            orderId = orderId,
                                            content = "Đơn hàng '${order.product?.ten_sp ?: ""}' đã được xác nhận",
                                            Notdate = Timestamp.now(),
                                            idUser = order.userId
                                        )

                                        notificationsRef.document(nextId.toString()).set(notification)
                                            .addOnSuccessListener {
                                                println("✅ Đã tạo thông báo với id $nextId")
                                            }
                                            .addOnFailureListener {
                                                println("❌ Lỗi tạo thông báo: ${it.message}")
                                            }
                                    } catch (e: Exception) {
                                        println("❌ Lỗi tạo thông báo: ${e.message}")
                                    }
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            println("Lỗi khi cập nhật trạng thái: ${exception.message}")
                        }
                } else {
                    println("Không tìm thấy đơn hàng với orderId: $orderId")
                }
            }.addOnFailureListener { exception ->
                println("Lỗi khi tìm kiếm đơn hàng: ${exception.message}")
            }
        }
    }

    private fun isSameDay(date1: Date?, date2: Date?): Boolean {
        if (date1 == null || date2 == null) return false
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
