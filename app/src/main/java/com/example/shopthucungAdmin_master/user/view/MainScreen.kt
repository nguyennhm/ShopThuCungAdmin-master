package com.example.shopthucungAdmin_master.user.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.shopthucungAdmin_master.user.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController, // Đây là NavController cấp cao để điều hướng toàn màn hình nếu cần
    viewModel: AdminViewModel = viewModel()
) {
    // NavController cho nội dung con
    val nestedNavController = rememberNavController()
    val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"

    Scaffold(

        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = {
                        nestedNavController.navigate("dashboard") {
                            popUpTo(nestedNavController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }) {
                        Icon(
                            Icons.Default.List,
                            contentDescription = "Sản phẩm",
                            tint = if (currentRoute == "dashboard") Color.Blue else Color.Gray
                        )
                    }
                    IconButton(onClick = {
                        nestedNavController.navigate("account") {
                            popUpTo(nestedNavController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Tài khoản",
                            tint = if (currentRoute == "account") Color.Blue else Color.Gray
                        )
                    }
                    IconButton(onClick = {
                        nestedNavController.navigate("orders") {
                            popUpTo(nestedNavController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = "Đặt hàng",
                            tint = if (currentRoute == "orders") Color.Blue else Color.Gray
                        )
                    }
                }
            }
        }
    ) { padding ->
        // NavHost cho nội dung động
        NavHost(
            navController = nestedNavController,
            startDestination = "dashboard",
            modifier = Modifier.padding(padding)
        ) {
            composable("dashboard") {
                AdminDashboardScreen(
                    navController = navController, // Truyền NavController cấp cao để điều hướng khi cần
                    viewModel = viewModel
                )
            }
            composable("orders") {
                OrderListScreen(navController = navController, viewModel = viewModel())
            }
            composable("account") {
                AccountScreen() // Tạo màn hình tài khoản (giả định)
            }
        }
    }
}

@Composable
fun AccountScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Màn hình tài khoản", style = MaterialTheme.typography.headlineMedium)
    }
}