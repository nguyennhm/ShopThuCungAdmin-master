package com.example.shopthucungAdmin_master.admin.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopthucungAdmin_master.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.Date
import java.util.UUID

class ProductViewModel(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val context: Context
) : ViewModel() {

    private val _product = MutableStateFlow<Product?>(null)
    val product: StateFlow<Product?> = _product

    fun loadProduct(tenSp: String) {
        viewModelScope.launch {
            try {
                val doc = firestore.collection("product").document(tenSp).get().await()
                val productData = doc.toObject(Product::class.java)
                if (productData != null) {
                    _product.value = productData
                } else {
                    Log.e("ProductViewModel", "Không tìm thấy sản phẩm với tên: $tenSp")
                }
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Lỗi khi load sản phẩm: ${e.message}")
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
        idCategory: Int,
        newImageUris: List<Uri>,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val newImageUrls = mutableListOf<String>()
                for (uri in newImageUris) {
                    val filePath = getRealPathFromUri(context, uri)
                    if (filePath != null) {
                        val imageUrl = CloudinaryUtils.uploadToCloudinary(uri, context)
                        if (imageUrl != null) {
                            newImageUrls.add(imageUrl)
                        } else {
                            Log.e("ProductViewModel", "Upload ảnh thất bại: $uri")
                        }
                    } else {
                        Log.e("ProductViewModel", "Không lấy được đường dẫn ảnh: $uri")
                    }
                }

                if (newImageUrls.size != newImageUris.size) {
                    Log.e("ProductViewModel", "Không upload đủ ảnh, huỷ lưu vào Firestore")
                    onComplete()
                    return@launch
                }

                val importDate = System.currentTimeMillis()

                saveProductToFirestore(
                    name, price, quantity,
                    description, discount,
                    idCategory, newImageUrls,
                    importDate, onComplete
                )
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Lỗi khi lưu sản phẩm: ${e.message}")
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
        idCategory: Int,
        newImageUrls: List<String>,
        importDate: Long,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val existingProduct = _product.value
                val existingImages = existingProduct?.anh_sp ?: emptyList()
                val allImages = existingImages + newImageUrls

                val oldQuantity = existingProduct?.soluong ?: 0
                val newId = existingProduct?.id_sanpham ?: getNextIdSanPham()

                val productToSave = Product(
                    ten_sp = name,
                    anh_sp = allImages,
                    gia_sp = price,
                    soluong = quantity,
                    mo_ta = description,
                    giam_gia = discount,
                    id_sanpham = newId,
                    so_luong_ban = existingProduct?.so_luong_ban ?: 0,
                    id_category = idCategory
                )

                // Ghi đè sản phẩm cũ bằng document id = name
                firestore.collection("product").document(name).set(productToSave).await()

                // Ghi lại thông tin nhập hàng nếu có số lượng tăng thêm
                val quantityToAdd = quantity - oldQuantity
                if (quantityToAdd > 0) {
                    val updateInfo = mapOf(
                        "ten_sp" to name,
                        "so_luong_nhap" to quantityToAdd,
                        "ngay_nhap" to Date(importDate)
                    )
                    firestore.collection("UpdateInfo").add(updateInfo).await()
                }

                // ✅ Nếu tên mới khác tên cũ => xóa document cũ theo tên cũ
                if (existingProduct != null && existingProduct.ten_sp != name) {
                    firestore.collection("product").document(existingProduct.ten_sp).delete().await()
                    Log.d("ProductViewModel", "Đã xoá sản phẩm cũ: ${existingProduct.ten_sp}")
                }

                onComplete()
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Lỗi khi lưu vào Firestore: ${e.message}")
                onComplete()
            }
        }
    }

    private suspend fun getNextIdSanPham(): Int {
        val querySnapshot = firestore.collection("product").get().await()
        val maxId = querySnapshot.documents.maxOfOrNull {
            it.getLong("id_sanpham")?.toInt() ?: 0
        } ?: 0
        return maxId + 1
    }

    private fun getRealPathFromUri(context: Context, uri: Uri): String? {
        val contentResolver = context.contentResolver
        val file = File.createTempFile("temp", UUID.randomUUID().toString(), context.cacheDir)
        return try {
            contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e("ProductViewModel", "Lỗi khi đọc file từ URI: ${e.message}")
            null
        }
    }
}
