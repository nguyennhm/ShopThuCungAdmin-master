package com.example.shopthucungAdmin_master.user.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.example.shopthucungAdmin_master.user.viewmodel.AdminViewModel
import kotlinx.coroutines.launch
import com.example.shopthucungAdmin_master.user.viewmodel.BannerViewModel
import com.example.shopthucungAdmin_master.user.view.BannerScreen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: AdminViewModel = viewModel()
) {
    val nestedNavController = rememberNavController()
    val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Các mục điều hướng
    // Cập nhật danh sách drawerItems
    val drawerItems = listOf(
        DrawerItem("dashboard", "Sản phẩm", Icons.Default.List),
        DrawerItem("orders", "Đơn hàng", Icons.Default.ShoppingCart),
        DrawerItem("banner", "Banner", Icons.Default.Image),
        DrawerItem("account", "Tài khoản", Icons.Default.AccountCircle)
    )


    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "Shop Thú Cưng",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )
                Spacer(Modifier.height(8.dp))

                drawerItems.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            nestedNavController.navigate(item.route) {
                                popUpTo(nestedNavController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                            // Đóng drawer sau khi chọn
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        val titleText = when (currentRoute) {
            "dashboard" -> "Sản phẩm"
            "orders" -> "Đơn hàng"
            "account" -> "Tài khoản"
            "banner" -> "Banner"
            else -> "Quản trị Shop"
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(titleText) },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        )
        { padding ->
            NavHost(
                navController = nestedNavController,
                startDestination = "dashboard",
                modifier = Modifier.padding(padding)
            ) {
                composable("dashboard") {
                    AdminDashboardScreen(
                        navController = navController,
                        viewModel = viewModel
                    )
                }
                composable("orders") {
                    OrderListScreen(navController = navController, viewModel = viewModel())
                }
                composable("account") {
                    AccountScreen()
                }
                composable("banner") {
                    BannerScreen(viewModel = viewModel<BannerViewModel>())
                }
            }
        }
    }
}

// Dữ liệu cho từng mục trong drawer
data class DrawerItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun AccountScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Màn hình tài khoản", style = MaterialTheme.typography.headlineMedium)
    }
}
