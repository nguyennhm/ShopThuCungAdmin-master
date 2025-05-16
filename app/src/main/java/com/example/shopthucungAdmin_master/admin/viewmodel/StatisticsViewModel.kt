package com.example.shopthucungAdmin_master.admin.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopthucungAdmin_master.model.Order
import com.example.shopthucungAdmin_master.model.Product
import com.example.shopthucungAdmin_master.model.UpdateInfo
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

data class Category(
    val id_category: Int = 0,
    val ten: String = ""
)

class StatisticsViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _updateSummary = MutableStateFlow<Map<String, Int>>(emptyMap())
    val updateSummary: StateFlow<Map<String, Int>> = _updateSummary

    private val _orderSummary = MutableStateFlow<Map<String, Long>>(emptyMap())
    val orderSummary: StateFlow<Map<String, Long>> = _orderSummary

    private val _updateDetailList = MutableStateFlow<List<UpdateInfo>>(emptyList())
    val updateDetailList: StateFlow<List<UpdateInfo>> = _updateDetailList

    private val _orderDetailList = MutableStateFlow<List<Order>>(emptyList())
    val orderDetailList: StateFlow<List<Order>> = _orderDetailList

    private val _categoryList = MutableStateFlow<List<Category>>(emptyList())
    val categoryList: StateFlow<List<Category>> = _categoryList

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            try {
                val categoryDocs = firestore.collection("category").get().await()
                val categories = categoryDocs.documents.mapNotNull {
                    val id = it.getLong("id_category")?.toInt() ?: return@mapNotNull null
                    val name = it.getString("ten") ?: return@mapNotNull null
                    Category(id_category = id, ten = name)
                }
                _categoryList.value = categories
            } catch (e: Exception) {
                Log.e("StatisticsVM", "Lỗi loadCategories: ${e.message}", e)
            }
        }
    }

    fun loadStatistics(
        selectedMonth: Int,
        selectedYear: Int,
        selectedCategory: Int?,
        loadAll: Boolean = false
    ) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val startDate: Date?
                val endDate: Date?

                if (loadAll) {
                    startDate = null
                    endDate = null
                } else {
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.YEAR, selectedYear)
                    calendar.set(Calendar.MONTH, selectedMonth - 1)
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    startDate = calendar.time

                    calendar.set(
                        Calendar.DAY_OF_MONTH,
                        calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                    )
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    calendar.set(Calendar.SECOND, 59)
                    calendar.set(Calendar.MILLISECOND, 999)
                    endDate = calendar.time
                }

                val productsQuery = if (selectedCategory != null) {
                    firestore.collection("product")
                        .whereEqualTo("id_category", selectedCategory)
                        .get().await()
                } else {
                    firestore.collection("product").get().await()
                }

                val productNames = productsQuery.documents.mapNotNull { it.getString("ten_sp") }

                if (productNames.isEmpty()) {
                    _updateSummary.value = emptyMap()
                    _orderSummary.value = emptyMap()
                    _updateDetailList.value = emptyList()
                    _orderDetailList.value = emptyList()
                    _loading.value = false
                    return@launch
                }

                val updateDocs = firestore.collection("UpdateInfo").get().await()
                val updateInfoList = updateDocs.documents.mapNotNull {
                    val tenSp = it.getString("ten_sp") ?: return@mapNotNull null
                    val date = it.getTimestamp("ngay_nhap")?.toDate()
                    val quantity = it.getLong("so_luong_nhap")?.toInt() ?: return@mapNotNull null

                    val isInDateRange = if (startDate != null && endDate != null && date != null) {
                        !date.before(startDate) && !date.after(endDate)
                    } else true

                    if (tenSp in productNames && isInDateRange) {
                        UpdateInfo(tenSp, quantity, date ?: Date())
                    } else null
                }

                _updateDetailList.value = updateInfoList
                _updateSummary.value = updateInfoList.groupBy { it.ten_sp }
                    .mapValues { it.value.sumOf { info -> info.so_luong_nhap } }

                val orderDocs = firestore.collection("orders").get().await()
                val orderList = orderDocs.documents.mapNotNull { doc ->
                    val product = doc.get("product") as? Map<*, *> ?: return@mapNotNull null
                    val tenSp = product["ten_sp"] as? String ?: return@mapNotNull null
                    val productId = (product["id"] as? Long)?.toInt() ?: 0
                    val bookingDate =
                        (doc.get("bookingDate") as? com.google.firebase.Timestamp)?.toDate()
                    val totalPrice = doc.getDouble("totalPrice")?.toLong() ?: return@mapNotNull null

                    val isInDateRangeOrder =
                        if (startDate != null && endDate != null && bookingDate != null) {
                            !bookingDate.before(startDate) && !bookingDate.after(endDate)
                        } else true

                    if (tenSp in productNames && isInDateRangeOrder) {
                        Order(
                            orderId = doc.id,
                            userId = doc.getString("userId") ?: "",
                            productId = productId,
                            product = null,
                            quantity = (doc.getLong("quantity") ?: 1L).toInt(),
                            totalPrice = totalPrice,
                            paymentMethod = doc.getString("paymentMethod") ?: "",
                            status = doc.getString("status") ?: "",
                            bookingdate = doc.getTimestamp("bookingDate"),
                            deliverydate = doc.getTimestamp("deliveryDate")
                        )
                    } else null
                }

                _orderDetailList.value = orderList
                _orderSummary.value = orderList.groupBy { it.productId.toString() }
                    .mapValues { it.value.sumOf { order -> order.totalPrice } }

            } catch (e: Exception) {
                Log.e("StatisticsVM", "Lỗi loadStatistics: ${e.message}", e)
            } finally {
                _loading.value = false
            }
        }
    }

}
