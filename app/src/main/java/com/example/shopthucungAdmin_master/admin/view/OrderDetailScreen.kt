package com.example.shopthucungAdmin_master.admin.view

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.shopthucungAdmin_master.admin.viewmodel.OrderViewModel
import com.example.shopthucungAdmin_master.model.Order
import com.example.shopthucungAdmin_master.utils.formatCurrency
import com.example.shopthucungAdmin_master.utils.formatTimestamp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

@Composable
fun OrderDetailScreen(orderId: String, navController: NavController, viewModel: OrderViewModel = viewModel()) {
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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

                val context = LocalContext.current
                val calendar = remember { Calendar.getInstance() }
                var showDatePicker by remember { mutableStateOf(false) }
                var selectedDate by remember {
                    mutableStateOf(orderData.deliverydate?.let { formatTimestamp(it) } ?: "")
                }

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
                            "Đang xử lý" -> Color(0xFFFFF59D)
                            "Đã xác nhận" -> Color(0xFF90CAF9)
                            "Đang giao hàng" -> Color(0xFFFFB74D)
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
                                listOf("Đang xử lý", "Đã xác nhận", "Đang giao hàng").forEach { status ->
                                    DropdownMenuItem(
                                        text = { Text(status) },
                                        onClick = {
                                            expanded = false
                                            currentStatus = status
                                            viewModel.updateOrderStatus(
                                                orderId = orderData.orderId,
                                                newStatus = status,
                                                productName = orderData.product?.ten_sp ?: "",
                                                status = null,
                                                bookingDate = orderData.bookingdate?.toDate()
                                            )
                                            order = orderData.copy(status = status)
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("🕒 Thời gian đặt: ${orderData.bookingdate?.let { formatTimestamp(it) } ?: "Không rõ"}")

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("📅 Ngày giao dự kiến:", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedButton(onClick = { showDatePicker = true }) {
                            Text(if (selectedDate.isNotEmpty()) selectedDate else "Chọn ngày")
                        }

                        if (showDatePicker) {
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    showDatePicker = false
                                    calendar.set(year, month, dayOfMonth)
                                    val timestamp = Timestamp(calendar.time)

                                    FirebaseFirestore.getInstance()
                                        .collection("orders")
                                        .document(orderId)
                                        .update("deliveryDate", timestamp)
                                        .addOnSuccessListener {
                                            selectedDate = formatTimestamp(timestamp)
                                            order = orderData.copy(deliverydate = timestamp)
                                            println("✅ Cập nhật ngày giao dự kiến")
                                        }
                                        .addOnFailureListener {
                                            println("❌ Lỗi cập nhật ngày giao: ${it.message}")
                                        }
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
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
