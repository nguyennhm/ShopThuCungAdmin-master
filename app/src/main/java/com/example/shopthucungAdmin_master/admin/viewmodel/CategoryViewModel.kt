package com.example.shopthucungAdmin_master.admin.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.shopthucungAdmin_master.model.Category
import com.google.firebase.firestore.FirebaseFirestore

class CategoryViewModel : ViewModel() {

    private val _categories = mutableStateOf<List<Category>>(emptyList())
    val categories: State<List<Category>> = _categories

    private val db = FirebaseFirestore.getInstance()

    init {
        fetchCategoriesRealtime()
    }

    private fun fetchCategoriesRealtime() {
        db.collection("category")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null && !snapshot.isEmpty) {
                    val categoryList = snapshot.documents.mapNotNull {
                        it.toObject(Category::class.java)
                    }
                    _categories.value = categoryList
                } else {
                    _categories.value = emptyList()
                }
            }
    }

    fun addCategory(category: Category) {
        db.collection("category")
            .document(category.id_category.toString())
            .set(category)
    }

    fun loadCategories() {
        FirebaseFirestore.getInstance().collection("category")
            .get()
            .addOnSuccessListener { result ->
                val list = result.documents.mapNotNull { it.toObject(Category::class.java) }
                _categories.value = list
            }
    }

    fun deleteCategory(id: String) {
        db.collection("category")
            .document(id)
            .delete()
    }

    fun updateStatus(id: String, newStatus: String) {
        db.collection("category")
            .document(id)
            .update("status", newStatus)
    }
}
