package com.example.shopthucungAdmin_master.user.view

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.shopthucungAdmin_master.user.viewmodel.ProductViewModel
import com.example.shopthucungAdmin_master.user.viewmodel.ProductViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    navController: NavController,
    productId: String? = null,
    viewModel: ProductViewModel = viewModel(factory = ProductViewModelFactory(context = LocalContext.current))
) {
    val product by viewModel.product.collectAsState()
    var name by remember { mutableStateOf(product?.ten_sp ?: "") }
    var price by remember { mutableStateOf(product?.gia_sp?.toString() ?: "") }
    var quantity by remember { mutableStateOf(product?.soluong?.toString() ?: "") }
    var description by remember { mutableStateOf(product?.mo_ta ?: "") }
    var discount by remember { mutableStateOf(product?.giam_gia?.toString() ?: "") }
    var newImageUris by remember { mutableStateOf(listOf<Uri>()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        if (productId != null) {
            viewModel.loadProduct(productId)
        } else {
            viewModel.clearProduct()
        }
    }

    // Cập nhật các giá trị khi product thay đổi (khi chỉnh sửa sản phẩm)
    LaunchedEffect(product) {
        product?.let {
            name = it.ten_sp
            price = it.gia_sp.toString()
            quantity = it.soluong.toString()
            description = it.mo_ta
            discount = it.giam_gia.toString()
            Log.d("ProductScreen", "Updated fields: name=$name, price=$price, quantity=$quantity, description=$description, discount=$discount")
        } ?: run {
            Log.d("ProductScreen", "Product is null, resetting fields")
            name = ""
            price = ""
            quantity = ""
            description = ""
            discount = ""
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        newImageUris = uris
        Log.d("ProductScreen", "Selected image URIs: $newImageUris")
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (productId == null) "Thêm sản phẩm" else "Chỉnh sửa sản phẩm") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tên sản phẩm") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Giá (VND)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Số lượng") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Mô tả") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = discount,
                onValueChange = { discount = it },
                label = { Text("Giảm giá (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Ảnh hiện có (nếu đang chỉnh sửa)
            product?.anh?.let { images ->
                if (images.isNotEmpty()) {
                    Text("Ảnh hiện có:")
                    LazyRow {
                        items(images) { imageUrl ->
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Ảnh sản phẩm",
                                modifier = Modifier.size(100.dp).padding(4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Ảnh mới
            Button(onClick = { launcher.launch("image/*") }) {
                Text("Chọn ảnh")
            }
            if (newImageUris.isNotEmpty()) {
                Text("Ảnh đã chọn:")
                LazyRow {
                    items(newImageUris) { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = "Ảnh đã chọn",
                            modifier = Modifier.size(100.dp).padding(4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { navController.navigate("admin_dashboard") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Quay lại")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        isLoading = true
                        viewModel.saveProduct(
                            name = name,
                            price = price.toLongOrNull() ?: 0L,
                            quantity = quantity.toIntOrNull() ?: 0,
                            description = description,
                            discount = discount.toIntOrNull() ?: 0,
                            newImageUris = newImageUris,
                            onComplete = {
                                isLoading = false
                                navController.popBackStack()
                            }
                        )
                    },
                    enabled = !isLoading && name.isNotBlank() && price.isNotBlank() && quantity.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isLoading) "Đang lưu..." else "Lưu")
                }
            }

        }
    }
}