package com.example.shopthucungAdmin_master.admin.view

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.shopthucungAdmin_master.admin.viewmodel.UpdateProductViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateQuantityScreen(
    navController: NavController,
    productName: String,
    viewModel: UpdateProductViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var quantity by remember { mutableStateOf("") }
    var importDateText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val updateSuccess by viewModel.updateSuccess.collectAsState()

    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Nhập hàng mới") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                label = { Text("Số lượng nhập thêm") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Trường nhập ngày có định dạng tự động
            OutlinedTextField(
                value = importDateText,
                onValueChange = {
                    val digits = it.filter { c -> c.isDigit() }.take(8)
                    importDateText = when {
                        digits.length >= 5 -> "${digits.substring(0, 2)}/${digits.substring(2, 4)}/${digits.substring(4)}"
                        digits.length >= 3 -> "${digits.substring(0, 2)}/${digits.substring(2)}"
                        else -> digits
                    }
                },
                label = { Text("Ngày nhập (dd/MM/yyyy)") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val qty = quantity.toIntOrNull()
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val importDate = try {
                        sdf.parse(importDateText)?.time ?: 0L
                    } catch (e: Exception) {
                        0L
                    }

                    if (qty != null && qty > 0 && importDate > 0L) {
                        viewModel.updateProductQuantity(productName, qty, importDate)
                    } else {
                        Toast.makeText(context, "Vui lòng nhập đúng thông tin", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cập nhật")
            }
        }
    }
}
