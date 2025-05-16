package com.example.shopthucungAdmin_master.admin.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.shopthucungAdmin_master.R
import com.example.shopthucungAdmin_master.admin.viewmodel.AdminViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    navController: NavController,
    viewModel: AdminViewModel = viewModel()
) {
    val products by viewModel.products.collectAsState()
    val totalRevenue by viewModel.totalRevenue.collectAsState()
    val totalQuantity by viewModel.totalQuantity.collectAsState()
    val totalQuantitySold by viewModel.totalQuantitySold.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var currentCardIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.fetchProducts()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("add_product")
            }) {
                Icon(Icons.Default.Add, contentDescription = "Thêm sản phẩm")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(8.dp)
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Card chuyển qua lại giữa doanh thu, tổng số lượng và số lượng bán
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            currentCardIndex = (currentCardIndex + 2) % 3
                        }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Previous",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            when (currentCardIndex) {
                                0 -> {
                                    Text("Doanh số", color = Color.White)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "${NumberFormat.getNumberInstance(Locale("vi", "VN")).format(totalRevenue)} VNĐ",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = Color.White
                                    )
                                }

                                1 -> {
                                    Text("Tổng số lượng", color = Color.White)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "$totalQuantity",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = Color.White
                                    )
                                }

                                2 -> {
                                    Text("Số lượng đã bán", color = Color.White)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "$totalQuantitySold",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = {
                            currentCardIndex = (currentCardIndex + 1) % 3
                        }) {
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = "Next",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tabs chuyển loại hiển thị
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF0F0F0), shape = RoundedCornerShape(8.dp))
            ) {
                TabButton(
                    title = "Tồn kho",
                    selected = selectedTab == 0,
                    modifier = Modifier.weight(1f)
                ) { selectedTab = 0 }

                TabButton(
                    title = "Đã bán",
                    selected = selectedTab == 1,
                    modifier = Modifier.weight(1f)
                ) { selectedTab = 1 }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val filteredProducts = when (selectedTab) {
                    0 -> products.filter { it.soluong - it.so_luong_ban > 0 }
                    1 -> products.filter { it.so_luong_ban > 0 }
                    else -> products
                }

                items(filteredProducts) { product ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate("edit_product/${product.ten_sp}")
                            },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val imageUrl = product.anh_sp.firstOrNull()
                            AsyncImage(
                                model = imageUrl ?: "",
                                contentDescription = product.ten_sp,
                                modifier = Modifier.size(50.dp),
                                error = painterResource(id = R.drawable.placeholder_image),
                                placeholder = painterResource(id = R.drawable.placeholder_image)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(product.ten_sp, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "Giá: ${NumberFormat.getNumberInstance(Locale("vi", "VN")).format(product.gia_sp)} VNĐ"
                                )
                                if (selectedTab == 0) {
                                    Text("Tồn kho: ${product.soluong - product.so_luong_ban}")
                                } else {
                                    Text("Đã bán: ${product.so_luong_ban}")
                                }
                            }
                            IconButton(onClick = {
                                navController.navigate("update_quantity/${product.ten_sp}")
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Thêm số lượng")
                            }
                            IconButton(onClick = {
                                viewModel.deleteProduct(product.ten_sp) { success ->
                                    println(if (success) "Xóa thành công" else "Xóa thất bại")
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Xóa")
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun TabButton(
    title: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val background = if (selected) Color.White else Color.Transparent
    val textColor = if (selected) Color.Black else Color.Gray

    Box(
        modifier = modifier
            .height(36.dp)
            .clickable { onClick() }
            .background(background, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(title, color = textColor)
    }
}