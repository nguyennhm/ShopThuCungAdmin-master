package com.example.shopthucungAdmin_master.user.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
        val snapshot = FirebaseFirestore.getInstance()
            .collection("order")
            .document(orderId)
            .get()
            .await()

        order = snapshot.toObject(Order::class.java)
    }

    order?.let {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Order Detail", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Mã đơn hàng: ${it.orderId}")
            Text("Sản phẩm: ${it.product?.ten_sp ?: "Không có thông tin"}")
            Text("Số lượng: ${it.quantity}")
            Text("Tổng tiền: ${formatCurrency(it.totalPrice)}")
            Text("Phương thức thanh toán: ${it.paymentMethod}")
            Text("Trạng thái: ${it.status}")
            Text("Thời gian: ${formatTimestamp(it.timestamp)}")
        }
    } ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
