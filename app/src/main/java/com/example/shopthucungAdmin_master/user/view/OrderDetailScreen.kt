package com.example.shopthucungAdmin_master.user.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.shopthucungAdmin_master.model.Order
import com.example.shopthucungAdmin_master.utils.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun OrderDetailScreen(orderId: String, navController: NavController) {
    var order by remember { mutableStateOf<Order?>(null) }

    LaunchedEffect(orderId) {
        try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("orders")
                .document(orderId)
                .get()
                .await()
            order = snapshot.toObject(Order::class.java)
        } catch (e: Exception) {
            println("Lỗi khi tải chi tiết đơn hàng: ${e.message}")
            order = null
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues()) // Tự động tránh các thanh trạng thái/điều hướng
    ) {
        Column {
            // Nút quay lại đẹp hơn
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Quay lại", color = Color.White)
                }
            }

            order?.let { orderData ->
                var currentStatus by remember { mutableStateOf(orderData.status) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Chi tiết đơn hàng", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("🆔 Mã đơn: ${orderData.orderId}")
                        Text("📦 Sản phẩm: ${orderData.product?.ten_sp ?: "Không có thông tin"}")
                        Text("🔢 Số lượng: ${orderData.quantity}")
                        Text("💰 Tổng tiền: ${formatCurrency(orderData.totalPrice)}")
                        Text("💳 Thanh toán: ${orderData.paymentMethod}")
                        Spacer(modifier = Modifier.height(12.dp))

                        Text("🚚 Trạng thái:", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(4.dp))

                        var expanded by remember { mutableStateOf(false) }

                        val backgroundColor = when (currentStatus) {
                            "Đang xử lý" -> Color(0xFFFFF176)
                            "Giao thành công" -> Color(0xFF81C784)
                            "Đã hủy" -> Color(0xFFE57373)
                            else -> Color.LightGray
                        }

                        Box {
                            Button(
                                onClick = { expanded = true },
                                colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
                                modifier = Modifier.width(200.dp)
                            ) {
                                Text(currentStatus, color = Color.Black)
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                listOf("Đang xử lý", "Giao thành công", "Đã hủy").forEach { status ->
                                    DropdownMenuItem(
                                        text = { Text(status) },
                                        onClick = {
                                            expanded = false
                                            currentStatus = status
                                            FirebaseFirestore.getInstance()
                                                .collection("orders")
                                                .document(orderId)
                                                .update("status", status)
                                                .addOnSuccessListener {
                                                    println("Đã cập nhật trạng thái: $status")
                                                    order = orderData.copy(status = status)
                                                }
                                                .addOnFailureListener { e ->
                                                    println("Lỗi cập nhật: ${e.message}")
                                                    currentStatus = orderData.status
                                                }
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("🕒 Thời gian: ${formatTimestamp(orderData.timestamp)}")
                    }
                }
            } ?: run {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

