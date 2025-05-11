package com.example.shopthucungAdmin_master.user.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.shopthucungAdmin_master.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID

class ProductViewModel(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val context: Context
) : ViewModel() {
    private val _product = MutableStateFlow<Product?>(null)
    val product: StateFlow<Product?> = _product

    init {
        val config = mapOf(
            "cloud_name" to "your_cloud_name",
            "api_key" to "your_api_key",
            "api_secret" to "your_api_secret"
        )
    }

    fun loadProduct(firestoreId: String) {
        viewModelScope.launch {
            try {
                // Truy vấn Firestore để lấy sản phẩm theo firestoreId
                val doc = firestore.collection("product").document(firestoreId).get().await()

                // In ra firestoreId và toàn bộ dữ liệu của sản phẩm
                Log.d("ProductViewModel", "Firestore ID: $firestoreId")
                Log.d("ProductViewModel", "Document data: ${doc.data}")  // In ra dữ liệu từ Firestore

                // Cập nhật giá trị _product với dữ liệu lấy được
                val productData = doc.toObject(Product::class.java)?.apply { this.firestoreId = doc.id }
                if (productData != null) {
                    _product.value = productData  // Đảm bảo cập nhật _product đúng cách
                } else {
                    Log.e("ProductViewModel", "No product data found for firestoreId: $firestoreId")
                }
            } catch (e: Exception) {
                // Xử lý lỗi và in thông báo lỗi
                Log.e("ProductViewModel", "Error loading product: ${e.message}")
            }
        }
    }



    fun clearProduct() {
        _product.value = null
    }

    fun saveProduct(
        name: String,
        price: Long,
        quantity: Int,
        description: String,
        discount: Int,
        newImageUris: List<Uri>,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val newImageUrls = mutableListOf<String>()
                newImageUris.forEach { uri ->
                    val filePath = getRealPathFromUri(context, uri)
                    if (filePath != null) {
                        MediaManager.get().upload(filePath)
                            .unsigned("your_upload_preset")
                            .callback(object : UploadCallback {
                                override fun onStart(requestId: String) {}
                                override fun onProgress(
                                    requestId: String,
                                    bytes: Long,
                                    totalBytes: Long
                                ) {
                                }

                                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                                    val url = resultData["secure_url"] as String
                                    newImageUrls.add(url)
                                    if (newImageUrls.size == newImageUris.size) {
                                        saveProductToFirestore(
                                            name,
                                            price,
                                            quantity,
                                            description,
                                            discount,
                                            newImageUrls,
                                            onComplete
                                        )
                                    }
                                }

                                override fun onError(requestId: String, error: ErrorInfo) {
                                    println("ProductViewModel: Error uploading image: ${error.description}")
                                    onComplete()
                                }

                                override fun onReschedule(requestId: String, error: ErrorInfo) {}
                            }).dispatch()
                    }
                }
                if (newImageUris.isEmpty()) {
                    saveProductToFirestore(
                        name,
                        price,
                        quantity,
                        description,
                        discount,
                        newImageUrls,
                        onComplete
                    )
                }
            } catch (e: Exception) {
                println("ProductViewModel: Error saving product: ${e.message}")
                onComplete()
            }
        }
    }

    private fun saveProductToFirestore(
        name: String,
        price: Long,
        quantity: Int,
        description: String,
        discount: Int,
        newImageUrls: List<String>,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val existingImages = _product.value?.anh ?: emptyList()
                val allImages = existingImages + newImageUrls

                if (_product.value == null) {
                    val newId = getNextIdSanPham()
                    val newProduct = Product(
                        anh = allImages,
                        ten_sp = name,
                        gia_sp = price,
                        soluong = quantity,
                        mo_ta = description,
                        giam_gia = discount,
                        id_sanpham = newId,
                        firestoreId = newId.toString(),
                        so_luong_ban = 0
                    )
                    val docRef = firestore.collection("product").add(newProduct).await()
                    newProduct.firestoreId = docRef.id
                    firestore.collection("product").document(docRef.id).set(newProduct).await()
                } else {
                    val updatedProduct = _product.value!!.copy(
                        anh = allImages,
                        ten_sp = name,
                        gia_sp = price,
                        soluong = quantity,
                        mo_ta = description,
                        giam_gia = discount
                    )
                    firestore.collection("product").document(updatedProduct.firestoreId)
                        .set(updatedProduct).await()
                }
                onComplete()
            } catch (e: Exception) {
                println("ProductViewModel: Error saving product to Firestore: ${e.message}")
                onComplete()
            }
        }
    }

    private suspend fun getNextIdSanPham(): Int {
        val querySnapshot = firestore.collection("product").get().await()
        val maxId =
            querySnapshot.documents.maxOfOrNull { it.getLong("id_sanpham")?.toInt() ?: 0 } ?: 0
        return maxId + 1
    }

    private fun getRealPathFromUri(context: Context, uri: Uri): String? {
        val contentResolver = context.contentResolver
        val file = File.createTempFile("temp", UUID.randomUUID().toString(), context.cacheDir)
        contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }
}