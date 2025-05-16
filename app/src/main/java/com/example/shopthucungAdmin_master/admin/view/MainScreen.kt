package com.example.shopthucungAdmin_master.admin.view

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
import com.example.shopthucungAdmin_master.admin.viewmodel.AdminViewModel
import com.example.shopthucungAdmin_master.admin.viewmodel.BannerViewModel
import com.example.shopthucungAdmin_master.admin.viewmodel.CategoryViewModel
import com.example.shopthucungAdmin_master.admin.viewmodel.UserViewModel
import kotlinx.coroutines.launch

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

    // Danh sách mục điều hướng trong drawer
    val drawerItems = listOf(
        DrawerItem("dashboard", "Sản phẩm", Icons.Default.List),
        DrawerItem("orders", "Đơn hàng", Icons.Default.ShoppingCart),
        DrawerItem("banner", "Banner", Icons.Default.Image),
        DrawerItem("category", "Danh mục", Icons.Default.Category),
        DrawerItem("users", "Tài khoản người dùng", Icons.Default.Person),
        DrawerItem("statistics", "Thống kê", Icons.Default.BarChart)
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
            "banner" -> "Banner"
            "category" -> "Danh mục"
            "users" -> "Tài khoản người dùng"
            "statistics" -> "Thống kê"
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
        ) { padding ->
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
                composable("banner") {
                    BannerScreen(viewModel = viewModel<BannerViewModel>())
                }
                composable("category") {
                    CategoryScreen(viewModel = viewModel<CategoryViewModel>())
                }
                composable("users") {
                    UserListScreen(viewModel = viewModel<UserViewModel>())
                }
                composable("statistics") {
                    StatisticsScreen()
                }
            }
        }
    }
}

data class DrawerItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

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
