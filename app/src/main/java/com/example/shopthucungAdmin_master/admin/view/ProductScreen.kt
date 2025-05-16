package com.example.shopthucungAdmin_master.admin.view

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.shopthucungAdmin_master.admin.viewmodel.ProductViewModel
import com.example.shopthucungAdmin_master.admin.viewmodel.ProductViewModelFactory
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.rememberScrollState
import com.example.shopthucungAdmin_master.admin.viewmodel.CategoryViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    navController: NavController,
    productName: String? = null,
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

    val categoryViewModel: CategoryViewModel = viewModel()
    val categories by categoryViewModel.categories
    var selectedCategory by remember { mutableStateOf(product?.id_category ?: "") }
    var selectedCategoryName by remember { mutableStateOf("") }

    LaunchedEffect(productName) {
        if (productName != null) {
            viewModel.loadProduct(productName)
        } else {
            viewModel.clearProduct()
        }
    }

    LaunchedEffect(product) {
        product?.let {
            name = it.ten_sp
            price = it.gia_sp.toString()
            quantity = it.soluong.toString()
            description = it.mo_ta
            discount = it.giam_gia.toString()
            selectedCategory = it.id_category
        }
    }

    LaunchedEffect(categories, selectedCategory) {
        categoryViewModel.loadCategories()
        val selected = categories.find { it.id_category == selectedCategory }
        selectedCategoryName = selected?.ten ?: ""
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        newImageUris = uris
    }

    fun removeImageByTenSP(tenSp: String, imageUrl: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("product")
            .whereEqualTo("ten_sp", tenSp)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    for (document in querySnapshot.documents) {
                        document.reference.update("anh_sp", FieldValue.arrayRemove(imageUrl))
                            .addOnSuccessListener {
                                Log.d("ProductScreen", "Đã xóa ảnh thành công.")
                                viewModel.loadProduct(tenSp)
                            }
                            .addOnFailureListener { e ->
                                Log.e("ProductScreen", "Lỗi khi xóa ảnh: ", e)
                            }
                    }
                } else {
                    Log.w("ProductScreen", "Không tìm thấy sản phẩm với tên $tenSp")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProductScreen", "Lỗi truy vấn Firestore: ", e)
            }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (productName == null) "Thêm sản phẩm" else "Chỉnh sửa sản phẩm") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState)
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

            Spacer(modifier = Modifier.height(8.dp))
            Text("Danh mục", fontSize = 16.sp)

            var expanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedCategoryName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Chọn danh mục") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor() // QUAN TRỌNG: giúp menu khớp vị trí
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.ten) },
                            onClick = {
                                selectedCategory = category.id_category
                                selectedCategoryName = category.ten
                                expanded = false
                            }
                        )
                    }
                }
            }


            product?.anh_sp?.let { images ->
                if (images.isNotEmpty()) {
                    Text("Ảnh sản phẩm đã lưu:", fontSize = 16.sp)
                    Column {
                        images.forEach { imageUrl ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier.size(100.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        product?.ten_sp?.let { tenSp ->
                                            removeImageByTenSP(tenSp, imageUrl)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Xoá ảnh",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { launcher.launch("image/*") }) {
                Text("Chọn ảnh thêm")
            }

            if (newImageUris.isNotEmpty()) {
                Text("Ảnh mới đã chọn:", fontSize = 16.sp)
                Column {
                    newImageUris.forEach { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(4.dp)
                                .size(100.dp)
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
                    onClick = { navController.popBackStack() },
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
                            idCategory = selectedCategory as Int,
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
