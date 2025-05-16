package com.example.shopthucungAdmin_master.admin.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // 🟢 import này rất quan trọng
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shopthucungAdmin_master.model.Category
import com.example.shopthucungAdmin_master.admin.viewmodel.CategoryViewModel

@Composable
fun CategoryScreen(viewModel: CategoryViewModel = viewModel()) {
    var newCategoryName by remember { mutableStateOf("") }
    var newStatus by remember { mutableStateOf("Bật") }

    val categoryList = viewModel.categories.value // ✅ Lấy danh sách thật

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Danh sách Danh mục", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(categoryList) { category ->
                var expanded by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // ✅ sửa tham số
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("ID: ${category.id_category}")
                            Text("Tên: ${category.ten}")
                        }

                        Box {
                            TextButton(onClick = { expanded = true }) {
                                Text(category.status)
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                listOf("Bật", "Tắt").forEach { statusOption ->
                                    DropdownMenuItem(
                                        text = { Text(statusOption) },
                                        onClick = {
                                            viewModel.updateStatus(category.id_category.toString(), statusOption)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        IconButton(onClick = {
                            viewModel.deleteCategory(category.id_category.toString())
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Xoá",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Thêm danh mục mới", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = newCategoryName,
            onValueChange = { newCategoryName = it },
            label = { Text("Tên danh mục") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (newCategoryName.isNotBlank()) {
                    val newId = (categoryList.maxOfOrNull { it.id_category } ?: 0) + 1
                    val newCategory = Category(newId, newCategoryName.trim(), newStatus)
                    viewModel.addCategory(newCategory)
                    newCategoryName = ""
                }
            }
        ) {
            Text("Thêm")
        }
    }
}
