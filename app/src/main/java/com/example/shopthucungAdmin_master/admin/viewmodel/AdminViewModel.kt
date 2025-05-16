package com.example.shopthucungAdmin_master.admin.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopthucungAdmin_master.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AdminViewModel(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _totalRevenue = MutableStateFlow(0L)
    val totalRevenue: StateFlow<Long> = _totalRevenue

    private val _totalProducts = MutableStateFlow(0)
    val totalProducts: StateFlow<Int> = _totalProducts

    private val _totalQuantity = MutableStateFlow(0)
    val totalQuantity: StateFlow<Int> = _totalQuantity

    private val _totalQuantitySold = MutableStateFlow(0)
    val totalQuantitySold: StateFlow<Int> = _totalQuantitySold

    private val _selectedCategory = MutableStateFlow(-1)
    val selectedCategory: StateFlow<Int> = _selectedCategory

    private val _selectedFilterType = MutableStateFlow("Tất cả") // "Tất cả", "Đã bán", "Tồn kho"
    val selectedFilterType: StateFlow<String> = _selectedFilterType

    fun updateSelectedCategory(categoryId: Int) {
        _selectedCategory.value = categoryId
    }

    fun updateSelectedFilterType(filterType: String) {
        _selectedFilterType.value = filterType
    }


    // ✅ Lọc sản phẩm theo ID danh mục (Int) và filter type
    val filteredProducts: StateFlow<List<Product>> = combine(
        products,
        selectedCategory,
        selectedFilterType
    ) { allProducts, categoryId, filterType ->
        allProducts.filter { product ->
            val matchCategory = categoryId == -1 || product.id_category == categoryId
            val matchFilterType = when (filterType) {
                "Tất cả" -> true
                "Đã bán" -> (product.so_luong_ban ?: 0) > 0
                "Tồn kho" -> (product.soluong ?: 0) > 0
                else -> true
            }
            matchCategory && matchFilterType
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    private var productListener: ListenerRegistration? = null

    init {
        fetchProducts()
        fetchTotalRevenue()
    }

    fun fetchProducts() {
        productListener?.remove()
        productListener = firestore.collection("product")
            .addSnapshotListener { snapshot, error ->
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        if (error != null) {
                            Log.e("AdminViewModel", "Error fetching products: ${error.message}")
                            return@withContext
                        }

                        val productList = snapshot?.documents?.mapNotNull { doc ->
                            try {
                                doc.toObject(Product::class.java)
                            } catch (e: Exception) {
                                Log.e("AdminViewModel", "Error parsing product: ${e.message}")
                                null
                            }
                        } ?: emptyList()

                        withContext(Dispatchers.Main) {
                            _products.value = productList
                            _totalProducts.value = productList.size
                            _totalQuantity.value = productList.sumOf { it.soluong ?: 0 }
                            _totalQuantitySold.value = productList.sumOf { it.so_luong_ban ?: 0 }
                        }
                    }
                }
            }
    }

    private fun fetchTotalRevenue() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val snapshot = firestore.collection("orders").get().await()
                    val total = snapshot.documents.sumOf { it.getLong("totalPrice") ?: 0L }
                    withContext(Dispatchers.Main) {
                        _totalRevenue.value = total
                    }
                } catch (e: Exception) {
                    Log.e("AdminViewModel", "Error fetching total revenue: ${e.message}")
                    withContext(Dispatchers.Main) {
                        _totalRevenue.value = 0L
                    }
                }
            }
        }
    }

    fun deleteProduct(tenSp: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val snapshot = firestore.collection("product")
                        .whereEqualTo("ten_sp", tenSp)
                        .get().await()

                    val documentId = snapshot.documents.firstOrNull()?.id
                    if (documentId != null) {
                        firestore.collection("product").document(documentId).delete().await()
                        Log.d("AdminViewModel", "Deleted product with ten_sp: $tenSp")
                        withContext(Dispatchers.Main) {
                            onComplete(true)
                        }
                    } else {
                        Log.d("AdminViewModel", "No product found with ten_sp: $tenSp")
                        withContext(Dispatchers.Main) {
                            onComplete(false)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AdminViewModel", "Error deleting product: ${e.message}")
                    withContext(Dispatchers.Main) {
                        onComplete(false)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        productListener?.remove()
        super.onCleared()
    }
}
