package com.example.shopthucungAdmin_master.user.view

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.shopthucungAdmin_master.model.Banner
import com.example.shopthucungAdmin_master.user.viewmodel.BannerViewModel
//import com.example.shopthucungAdmin_master.utils.CloudinaryUtils
import kotlinx.coroutines.launch

@Composable
fun BannerScreen(viewModel: BannerViewModel = viewModel()) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var newStatus by remember { mutableStateOf("Bật") }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                val uploadedUrl = CloudinaryUtils.uploadToCloudinary(it, context)
                if (uploadedUrl != null) {
                    val newId = (viewModel.banners.maxOfOrNull { it.id_banner } ?: 0) + 1
                    viewModel.addBanner(Banner(newId, uploadedUrl, newStatus))
                }
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Danh sách Banner", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(viewModel.banners.size) { index ->
                val banner = viewModel.banners[index]
                var expanded by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ID: ${banner.id_banner}", modifier = Modifier.weight(1f))

                        AsyncImage(
                            model = banner.anh_banner,
                            contentDescription = null,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Box {
                            TextButton(onClick = { expanded = true }) {
                                Text(banner.status)
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                listOf("Bật", "Tắt").forEach {
                                    DropdownMenuItem(
                                        text = { Text(it) },
                                        onClick = {
                                            viewModel.updateStatus(banner.id_banner, it)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        IconButton(onClick = { viewModel.deleteBanner(banner.id_banner) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Xoá", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Thêm banner mới", fontWeight = FontWeight.Bold)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Trạng thái:")
            Spacer(modifier = Modifier.width(8.dp))

            var expandedStatus by remember { mutableStateOf(false) }
            Box {
                TextButton(onClick = { expandedStatus = true }) {
                    Text(newStatus)
                }
                DropdownMenu(expanded = expandedStatus, onDismissRequest = { expandedStatus = false }) {
                    listOf("Bật", "Tắt").forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                newStatus = it
                                expandedStatus = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(onClick = { launcher.launch("image/*") }) {
                Text("Chọn ảnh")
            }
        }
    }
}
