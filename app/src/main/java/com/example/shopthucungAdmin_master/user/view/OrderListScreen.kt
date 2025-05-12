package com.example.shopthucungAdmin_master.user.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.shopthucungAdmin_master.user.viewmodel.OrderViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderListScreen(
    navController: NavController,
    viewModel: OrderViewModel = viewModel()
) {
    val orders by viewModel.filteredOrders.collectAsState()
    val context = LocalContext.current

    var ordersFrom by remember { mutableStateOf("11/01/2017") }
    var ordersTill by remember { mutableStateOf("11/16/2018") }
    var selectedStatus by remember { mutableStateOf<String?>(null) }
    var searchOrderId by remember { mutableStateOf("") }
    var selectedPayment by remember { mutableStateOf<String?>(null) }
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val total = orders.size
    val totalProcessing = orders.count { it.status == "Đang xử lý" }
    var totalDelivered by remember { mutableStateOf(orders.count { it.status == "Giao thành công" }) }
    val totalCanceled = orders.count { it.status == "Đã hủy" }

    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    sdf.isLenient = false // Đảm bảo kiểm tra định dạng nghiêm ngặt

    // Cập nhật bộ lọc khi có thay đổi
    LaunchedEffect(ordersFrom, ordersTill, selectedStatus, searchOrderId, selectedPayment, selectedDate) {
        // Kiểm tra định dạng ngày
        val fromDate = try {
            if (ordersFrom.isNotBlank()) sdf.parse(ordersFrom) else null
        } catch (e: Exception) {
            errorMessage = "Định dạng ngày 'Từ ngày' không hợp lệ (dd/MM/yyyy)"
            null
        }

        val toDate = try {
            if (ordersTill.isNotBlank()) sdf.parse(ordersTill) else null
        } catch (e: Exception) {
            errorMessage = "Định dạng ngày 'Đến ngày' không hợp lệ (dd/MM/yyyy)"
            null
        }

        if (fromDate != null && toDate != null && fromDate.after(toDate)) {
            errorMessage = "'Từ ngày' phải nhỏ hơn hoặc bằng 'Đến ngày'"
        } else if (errorMessage != null) {
            errorMessage = null
        }

        viewModel.applyFilters(searchOrderId, selectedStatus, selectedDate, ordersFrom, ordersTill)
    }

    LaunchedEffect(Unit) {
        viewModel.fetchOrders()
    }

    Column(modifier = Modifier.fillMaxSize().padding(5.dp)) {
        // Thanh lọc trên cùng
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Orders From và Orders Till
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = ordersFrom,
                    onValueChange = { ordersFrom = it },
                    label = { Text("Từ ngày") },
                    modifier = Modifier
                        .width(120.dp)
                        .height(56.dp),
                    placeholder = { Text("dd/MM/yyyy") }
                )

                OutlinedTextField(
                    value = ordersTill,
                    onValueChange = { ordersTill = it },
                    label = { Text("Đến ngày") },
                    modifier = Modifier
                        .width(120.dp)
                        .height(56.dp),
                    placeholder = { Text("dd/MM/yyyy") }
                )
            }
        }

        // Hiển thị thông báo lỗi nếu có
        errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Thống kê
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusStat("Tổng: $total", Color.LightGray, Modifier.weight(1f))
            StatusStat("Đang xử lý: $totalProcessing", Color(0xFFFFF176), Modifier.weight(1f))
            StatusStat("Giao thành công: $totalDelivered", Color(0xFF81C784), Modifier.weight(1f))
            StatusStat("Đã hủy: $totalCanceled", Color(0xFFE57373), Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bảng danh sách đơn hàng
        LazyColumn {
            // Header bảng
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF003087))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Name", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(3f))
                    Text("Method", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                    Text("Date", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("Status", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(5f))
                }
            }

            // Nội dung bảng
            items(orders) { order ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            navController.navigate("order_detail/${order.orderId}")
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(order.product?.ten_sp ?: "N/A", modifier = Modifier.weight(1f))
                    Text(order.paymentMethod, modifier = Modifier.weight(1f))
                    Text(
                        SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                            .format(order.timestamp.toDate()),
                        modifier = Modifier.weight(1f)
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        var statusExpanded by remember { mutableStateOf(false) }
                        val borderColor = when (order.status) {
                            "Đang xử lý" -> Color(0xFFFFF176) // Màu vàng nhạt
                            "Giao thành công" -> Color(0xFF81C784) // Màu xanh lá nhạt
                            "Đã hủy" -> Color(0xFFE57373) // Màu đỏ nhạt
                            else -> Color.Gray // Mặc định
                        }
                        OutlinedButton(
                            onClick = { statusExpanded = true },
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                width = 1.dp,
                                brush = SolidColor(borderColor)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(order.status, color = Color.Black)
                        }
                        DropdownMenu(
                            expanded = statusExpanded,
                            onDismissRequest = { statusExpanded = false }
                        ) {
                            listOf("Đang xử lý", "Giao thành công", "Đã hủy").forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status) },
                                    onClick = {
                                        statusExpanded = false
                                        viewModel.updateOrderStatus(
                                            order.orderId,
                                            status,
                                            searchOrderId,
                                            selectedStatus,
                                            selectedDate
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusStat(text: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.padding(4.dp),
        color = color,
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(Modifier.padding(8.dp)) {
            Text(text = text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}