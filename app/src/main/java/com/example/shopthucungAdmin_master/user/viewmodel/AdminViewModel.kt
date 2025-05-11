package com.example.shopthucungAdmin_master.user.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopthucungAdmin_master.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
                            println("AdminViewModel: Error fetching products: ${error.message}")
                            return@withContext
                        }

                        val productList = snapshot?.documents?.mapNotNull { doc ->
                            try {
                                val product = doc.toObject(Product::class.java)
                                product?.firestoreId = doc.id
                                product
                            } catch (e: Exception) {
                                println("AdminViewModel: Deserialization error for document ${doc.id}: ${e.message}")
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
                    println("AdminViewModel: Error fetching total revenue: ${e.message}")
                    withContext(Dispatchers.Main) {
                        _totalRevenue.value = 0L
                    }
                }
            }
        }
    }

    fun deleteProduct(productId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    firestore.collection("product").document(productId).delete().await()
                    println("AdminViewModel: Product deleted successfully: $productId")
                    withContext(Dispatchers.Main) {
                        onComplete(true)
                    }
                } catch (e: Exception) {
                    println("AdminViewModel: Error deleting product $productId: ${e.message}")
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