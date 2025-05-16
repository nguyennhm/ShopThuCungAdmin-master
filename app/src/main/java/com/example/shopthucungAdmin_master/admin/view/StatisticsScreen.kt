package com.example.shopthucungAdmin_master.admin.view

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shopthucungAdmin_master.admin.viewmodel.Category
import com.example.shopthucungAdmin_master.admin.viewmodel.StatisticsViewModel
import com.example.shopthucungAdmin_master.model.Order
import com.example.shopthucungAdmin_master.model.UpdateInfo
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatisticsScreen(
    statisticsViewModel: StatisticsViewModel = viewModel()
) {
    val updateData by statisticsViewModel.updateSummary.collectAsState()
    val orderData by statisticsViewModel.orderSummary.collectAsState()
    val loading by statisticsViewModel.loading.collectAsState()
    val updateDetails by statisticsViewModel.updateDetailList.collectAsState()
    val orderDetails by statisticsViewModel.orderDetailList.collectAsState()
    val categories by statisticsViewModel.categoryList.collectAsState()

    var selectedMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH) + 1) }
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var selectedCategory by remember { mutableStateOf<Int?>(null) }
    var selectedCategoryName by remember { mutableStateOf("Tất cả") }

    val selectedMonthYearDisplay = "Tháng ${selectedMonth.toString().padStart(2, '0')} / $selectedYear"

    LaunchedEffect(Unit) {
        // ✅ Load toàn bộ dữ liệu ngay khi vào màn hình
        statisticsViewModel.loadStatistics(
            selectedMonth = 0,
            selectedYear = 0,
            selectedCategory = null,
            loadAll = true
        )
    }


    Column(
        Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .fillMaxHeight()
    ) {
        // Hàng chọn tháng, năm, danh mục, lọc
        Row(verticalAlignment = Alignment.CenterVertically) {
            DropdownMenuBox(
                label = "Tháng: ${selectedMonth.toString().padStart(2, '0')}",
                options = (1..12).map { it.toString().padStart(2, '0') },
                selected = selectedMonth.toString().padStart(2, '0'),
                onSelected = { selectedMonth = it.toInt() }
            )

            Spacer(Modifier.width(8.dp))

            DropdownMenuBox(
                label = "Năm: $selectedYear",
                options = (2023..Calendar.getInstance().get(Calendar.YEAR)).map { it.toString() },
                selected = selectedYear.toString(),
                onSelected = { selectedYear = it.toInt() }
            )

            Spacer(Modifier.width(8.dp))

            DropdownMenuBox(
                label = selectedCategoryName,
                options = listOf("Tất cả") + categories.map { it.ten },
                selected = selectedCategoryName,
                onSelected = { selectedName ->
                    selectedCategoryName = selectedName
                    selectedCategory = if (selectedName == "Tất cả") null
                    else categories.find { it.ten == selectedName }?.id_category
                }
            )

            Spacer(Modifier.width(8.dp))

            Button(onClick = {
                statisticsViewModel.loadStatistics(selectedMonth, selectedYear, selectedCategory)
            }) {
                Text("Lọc")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Nút Tất cả riêng biệt bên dưới
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                selectedCategory = null
                selectedCategoryName = "Tất cả"
                statisticsViewModel.loadStatistics(
                    selectedMonth,
                    selectedYear,
                    null,
                    loadAll = true
                )
            }
        ) {
            Text("Tất cả - Hiển thị toàn bộ dữ liệu")
        }

        Spacer(Modifier.height(20.dp))


        if (loading) {
            CircularProgressIndicator()
        } else {
            Text("📦 Sản phẩm nhập:", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)

//            updateData.forEach { (tenSp, qty) ->
//                Text("- $tenSp: $qty")
//            }

            DrawBarChartByDateWithQuantityOnly(
                dataList = updateDetails,
                color = Color.Blue
            )

            Spacer(Modifier.height(16.dp))

            Text("🛒 Sản phẩm bán:", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)

//            orderData.forEach { (tenSp, qty) ->
//                Text("- $tenSp: $qty")
//            }

            DrawBarChartByDateWithMoneyAndQuantity(
                dataList = orderDetails,
                color = Color.Red
            )
        }
    }
}


@Composable
fun DropdownMenuBox(label: String, options: List<String>, selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(onClick = { expanded = true }) {
            Text(label)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach {
                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = {
                        onSelected(it)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DrawBarChartByDateWithMoneyAndQuantity(
    dataList: List<Order>,
    color: Color
) {
    if (dataList.isEmpty()) {
        Box(
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth()
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text("Không có dữ liệu đơn hàng", color = Color.Black, fontSize = 16.sp)
        }
        return
    }

    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val groupedData = dataList.groupBy { order ->
        dateFormatter.format(order.bookingdate?.toDate() ?: Date())
    }

    val totalMoneyByDate = groupedData.mapValues { entry ->
        entry.value.sumOf { it.totalPrice }
    }

    val quantityByDate = groupedData.mapValues { entry ->
        entry.value.sumOf { it.quantity }
    }

    val maxValue = totalMoneyByDate.values.maxOrNull()?.takeIf { it > 0 } ?: 1

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.Bottom
    ) {
        totalMoneyByDate.forEach { (date, money) ->
            val heightRatio = money.toFloat() / maxValue
            val quantity = quantityByDate[date] ?: 0
            val moneyInThousands = money / 1000

            Column(
                modifier = Modifier
                    .width(70.dp)
                    .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text("$quantity SP", fontSize = 12.sp, color = Color.DarkGray)

                Box(
                    modifier = Modifier
                        .height((heightRatio * 150).dp)
                        .width(40.dp)
                        .background(color)
                )

                Text(date, fontSize = 12.sp, maxLines = 1)
                Text("${moneyInThousands}k", fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DrawBarChartByDateWithQuantityOnly(
    dataList: List<UpdateInfo>,
    color: Color
) {
    if (dataList.isEmpty()) {
        Box(
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth()
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text("Không có dữ liệu nhập hàng", color = Color.Black, fontSize = 16.sp)
        }
        return
    }

    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val groupedData = dataList.groupBy { update ->
        dateFormatter.format(update.ngay_nhap)
    }

    val quantityByDate = groupedData.mapValues { entry ->
        entry.value.sumOf { it.so_luong_nhap }
    }

    val maxValue = quantityByDate.values.maxOrNull()?.takeIf { it > 0 } ?: 1

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.Bottom
    ) {
        quantityByDate.forEach { (date, quantity) ->
            val heightRatio = quantity.toFloat() / maxValue

            Column(
                modifier = Modifier
                    .width(70.dp)
                    .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text("$quantity SP", fontSize = 12.sp, color = Color.DarkGray)

                Box(
                    modifier = Modifier
                        .height((heightRatio * 150).dp)
                        .width(40.dp)
                        .background(color)
                )

                Text(date, fontSize = 12.sp, maxLines = 1)
            }
        }
    }
}