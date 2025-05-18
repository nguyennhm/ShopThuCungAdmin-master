package com.example.shopthucungAdmin_master.admin.view

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.shopthucungAdmin_master.admin.viewmodel.OrderViewModel
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
    var shouldApplyFilter by remember { mutableStateOf(false) }

    val total = orders.size
    val totalProcessing = orders.count { it.status == "Đang xử lý" }
    var totalDelivered by remember { mutableStateOf(orders.count { it.status == "Giao thành công" }) }
    val totalCanceled = orders.count { it.status == "Đã hủy" }

    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    sdf.isLenient = false

    LaunchedEffect(shouldApplyFilter) {
        if (shouldApplyFilter) {
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
            } else {
                viewModel.applyFilters(
                    searchOrderId,
                    selectedStatus,
                    selectedDate,
                    ordersFrom,
                    ordersTill
                )
            }
            shouldApplyFilter = false
        }
    }

    LaunchedEffect(Unit) {
        try {
            viewModel.fetchOrders()
        } catch (e: Exception) {
            println("Lỗi khi fetch orders: ${e.message}")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp)
    ) {
        // --- Thanh lọc ngày ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(Color(0xFFF1F1F1), shape = RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Nút chọn ngày "Từ ngày" (giống OrderDetailScreen)
                val calendar = Calendar.getInstance()
                val fromDatePickerDialog = DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        calendar.set(year, month, dayOfMonth)
                        ordersFrom = sdf.format(calendar.time)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )

                OutlinedButton(
                    onClick = { fromDatePickerDialog.show() },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text(if (ordersFrom.isNotEmpty()) ordersFrom else "Chọn ngày")
                }

                // Nút chọn ngày "Đến ngày" (giống OrderDetailScreen)
                val tillDatePickerDialog = DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        calendar.set(year, month, dayOfMonth)
                        ordersTill = sdf.format(calendar.time)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )

                OutlinedButton(
                    onClick = { tillDatePickerDialog.show() },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text(if (ordersTill.isNotEmpty()) ordersTill else "Chọn ngày")
                }

                Button(
                    onClick = { shouldApplyFilter = true },
                    modifier = Modifier
                        .height(56.dp)
                        .padding(top = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Lọc", fontSize = 14.sp)
                }
            }

            // Thông báo lỗi nếu có
            errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Thống kê trạng thái đơn hàng ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusStat("Tổng\n$total", Color.LightGray, Modifier.weight(1f))
            StatusStat("Đang xử lý\n$totalProcessing", Color(0xFFFFF59D), Modifier.weight(1f))
            StatusStat("Đã xác nhận\n$totalDelivered", Color(0xFF90CAF9), Modifier.weight(1f))
            StatusStat("Đang giao\n$totalCanceled", Color(0xFFFFB74D), Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bảng danh sách đơn hàng
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
        ) {
            items(orders) { order ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate("order_detail/${order.orderId}")
                        },
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Mã đơn: ${order.orderId}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Sản phẩm: ${order.product?.ten_sp ?: "Không rõ"}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("Số lượng", fontSize = 12.sp, color = Color.Gray)
                                Text("${order.quantity}", fontSize = 14.sp)
                            }

                            Column {
                                Text("Tổng tiền", fontSize = 12.sp, color = Color.Gray)
                                Text("${order.totalPrice} đ", fontSize = 14.sp)
                            }

                            Column {
                                Text("Thanh toán", fontSize = 12.sp, color = Color.Gray)
                                Text(order.paymentMethod, fontSize = 14.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("Ngày", fontSize = 12.sp, color = Color.Gray)
                                Text(
                                    text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                        .format(order.bookingdate?.toDate() ?: Date()),
                                    fontSize = 14.sp
                                )
                            }

                            Column {
                                Text("Trạng thái", fontSize = 12.sp, color = Color.Gray)
                                Box {
                                    val currentStatus = order.status
                                    val backgroundColor = when (currentStatus) {
                                        "Đang xử lý" -> Color(0xFFFFF59D)
                                        "Đã xác nhận" -> Color(0xFF90CAF9)
                                        "Đang giao hàng" -> Color(0xFFFFB74D)
                                        "Giao thành công" -> Color(0xFF81C784)
                                        "Đã hủy" -> Color(0xFFE57373)
                                        else -> Color.LightGray
                                    }

                                    var statusExpanded by remember { mutableStateOf(false) }

                                    if (currentStatus != "Giao thành công" && currentStatus != "Đã hủy") {
                                        Button(
                                            onClick = { statusExpanded = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            Text(currentStatus, color = Color.Black)
                                        }

                                        DropdownMenu(
                                            expanded = statusExpanded,
                                            onDismissRequest = { statusExpanded = false }
                                        ) {
                                            listOf(
                                                "Đang xử lý",
                                                "Đã xác nhận",
                                                "Đang giao hàng",
                                            ).forEach { status ->
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
                                    } else {
                                        // Hiển thị trạng thái không tương tác
                                        Button(
                                            onClick = {}, // Không làm gì
                                            enabled = false,
//                                            colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            Text(currentStatus, color = Color.Black)
                                        }
                                    }
                                }
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
        modifier = modifier,
        color = color,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 8.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = Color.Black
            )
        }
    }
}