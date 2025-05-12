package com.example.shopthucungAdmin_master.user.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopthucungAdmin_master.model.UpdateInfo
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class UpdateProductViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess = _updateSuccess.asStateFlow()

    fun updateProductQuantity(productName: String, quantityToAdd: Int, importDate: Long) {
        viewModelScope.launch {
            val productRef = db.collection("product").document(productName)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(productRef)
                val currentQty = snapshot.getLong("soluong") ?: 0
                val newQty = currentQty + quantityToAdd
                transaction.update(productRef, "soluong", newQty)
            }.addOnSuccessListener {
                val updateInfo = UpdateInfo(
                    ten_sp = productName,
                    so_luong_nhap = quantityToAdd,
                    ngay_nhap = Date(importDate)
                )

                db.collection("UpdateInfo").add(updateInfo)
                    .addOnSuccessListener {
                        _updateSuccess.value = true
                    }
                    .addOnFailureListener {
                        _updateSuccess.value = false
                    }

            }.addOnFailureListener {
                _updateSuccess.value = false
            }
        }
    }
}
