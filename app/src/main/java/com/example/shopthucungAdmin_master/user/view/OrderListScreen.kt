package com.example.shopthucungAdmin_master.user.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.shopthucungAdmin_master.model.Order
import com.example.shopthucungAdmin_master.user.viewmodel.OrderViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OrderListScreen(navController: NavController, viewModel: OrderViewModel = OrderViewModel()) {
    val orders by viewModel.orders.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchOrders()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Orders history", style = MaterialTheme.typography.headlineSmall)

        LazyColumn {
            items(orders) { order ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            navController.navigate("order_detail/${order.orderId}")
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Order number
                        Text(
                            text = "#${order.orderId}",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )

                        // Status
                        Text(
                            text = order.status,
                            color = when (order.status.lowercase()) {
                                "completed" -> Color(0xFF4CAF50) // Green
                                "pending" -> Color(0xFFFFC107) // Orange
                                else -> Color(0xFFF44336) // Red
                            },
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )

                        // Date and Time
                        Text(
                            text = formatTimestamp(order.timestamp),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.weight(1f)
                        )

                        // Amount
                        Text(
                            text = "${formatCurrency(order.totalPrice)}",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )

                        // More icon (optional, like in the image)
                        IconButton(
                            onClick = { /* Handle more options */ },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

fun formatCurrency(value: Long): String {
    return "%,d VNĐ".format(value).replace(',', '.')
}

fun formatTimestamp(timestamp: com.google.firebase.Timestamp): String {
    val date = timestamp.toDate()
    val formatter = SimpleDateFormat("MM/dd/yyyy 'at' h:mm a", Locale.getDefault())
    return formatter.format(date)
}