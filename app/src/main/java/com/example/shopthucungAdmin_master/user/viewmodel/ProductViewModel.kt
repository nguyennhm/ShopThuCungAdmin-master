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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class ProductViewModel(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val context: Context
) : ViewModel() {
    private val _product = MutableStateFlow<Product?>(null)
    val product: StateFlow<Product?> = _product

    init {
        // Bạn có thể khởi tạo CloudinaryUtils tại đây nếu cần
    }

    fun loadProduct(tenSp: String) {
        viewModelScope.launch {
            try {
                val doc = firestore.collection("product").document(tenSp).get().await()
                Log.d("ProductViewModel", "Tên sản phẩm: $tenSp")
                Log.d("ProductViewModel", "Dữ liệu sản phẩm: ${doc.data}")
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
        newImageUris: List<Uri>,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val newImageUrls = mutableListOf<String>()
                for (uri in newImageUris) {
                    val filePath = getRealPathFromUri(context, uri)
                    if (filePath != null) {
                        val imageUrl = CloudinaryUtils.uploadToCloudinary(uri, context) // Sử dụng CloudinaryUtils ở đây
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

                // Upload xong mới lưu
                saveProductToFirestore(
                    name, price, quantity,
                    description, discount, newImageUrls, onComplete
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
        newImageUrls: List<String>,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val existingImages = _product.value?.anh_sp ?: emptyList()
                val allImages = existingImages + newImageUrls

                val productToSave: Product

                if (_product.value == null) {
                    val newId = getNextIdSanPham()
                    productToSave = Product(
                        ten_sp = name,
                        anh_sp = allImages,
                        gia_sp = price,
                        soluong = quantity,
                        mo_ta = description,
                        giam_gia = discount,
                        id_sanpham = newId,
                        so_luong_ban = 0
                    )
                } else {
                    productToSave = _product.value!!.copy(
                        anh_sp = allImages,
                        ten_sp = name,
                        gia_sp = price,
                        soluong = quantity,
                        mo_ta = description,
                        giam_gia = discount
                    )
                }

                Log.d("ProductViewModel", "Lưu dữ liệu Firestore: $productToSave")
                firestore.collection("product").document(name).set(productToSave).await()
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
