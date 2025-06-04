package com.example.shopthucungAdmin_master.admin.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shopthucungAdmin_master.admin.viewmodel.UserViewModel

@Composable
fun UserListScreen(viewModel: UserViewModel = viewModel()) {
    val users by viewModel.users.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Danh sách người dùng", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(users.size) { index ->
                val user = users[index]
                var expanded by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Họ tên: ${user.hoVaTen}")
                        Text("Email: ${user.email}")
                        Text("Quyền: ${user.role}")

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Trạng thái: ${if (user.active) "Đang hoạt động" else "Bị khóa"}")

                            if (user.role == "user") {
                                DropdownMenuBox(user, viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DropdownMenuBox(
    user: com.example.shopthucungAdmin_master.model.User,
    viewModel: UserViewModel
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box {
        TextButton(onClick = { expanded = true }) {
            Text(if (user.active) "Mở" else "Khóa")
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf("Mở" to true, "Khóa" to false).forEach { (label, value) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        viewModel.updateUserActiveStatus(
                            user.idUser,
                            value,
                            context
                        )

                        expanded = false
                    }
                )
            }
        }
    }
}
