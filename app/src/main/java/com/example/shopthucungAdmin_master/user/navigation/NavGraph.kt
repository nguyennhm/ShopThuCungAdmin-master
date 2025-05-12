package com.example.shopthucungAdmin_master.user.navigation

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.shopthucungAdmin_master.user.view.AdminDashboardScreen
import com.example.shopthucungAdmin_master.user.view.LoginScreen
import com.example.shopthucungAdmin_master.user.view.OrderDetailScreen
import com.example.shopthucungAdmin_master.user.view.OrderListScreen
import com.example.shopthucungAdmin_master.user.view.ProductScreen
import com.example.shopthucungAdmin_master.user.view.RegisterScreen
import com.example.shopthucungAdmin_master.user.view.UpdateQuantityScreen
import com.example.shopthucungAdmin_master.user.viewmodel.LoginViewModel
import com.example.shopthucungAdmin_master.user.viewmodel.RegisterViewModel
import com.google.firebase.firestore.FirebaseFirestore

@SuppressLint("ContextCastToActivity")
@Composable
fun NavGraph(navController: NavHostController) {
    val firestore = FirebaseFirestore.getInstance()
    val loginViewModel = LoginViewModel(firestore)
    val registerViewModel = RegisterViewModel(firestore)

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(navController = navController, viewModel = loginViewModel)
        }
        composable("register") {
            RegisterScreen(navController = navController, viewModel = registerViewModel)
        }
        composable("admin_dashboard") {
            AdminDashboardScreen(navController = navController)
        }
        composable("add_product") {
            ProductScreen(navController = navController)
        }
        composable(
            route = "edit_product/{ten_sp}",
            arguments = listOf(navArgument("ten_sp") { type = NavType.StringType })
        ) { backStackEntry ->
            val productName = backStackEntry.arguments?.getString("ten_sp")
            ProductScreen(navController = navController, productName = productName)
        }
        composable("products") {
            // Placeholder cho màn hình Sản phẩm, hiện tại quay lại admin_dashboard
            AdminDashboardScreen(navController = navController)
        }
        composable("account") {
            // Placeholder cho màn hình Tài khoản, hiện tại quay lại admin_dashboard
            AdminDashboardScreen(navController = navController)
        }
        composable("orders") {
            OrderListScreen(navController = navController)
        }
        composable(
            route = "order_detail/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            OrderDetailScreen(orderId = orderId, navController = navController)
        }
        composable("update_quantity/{productName}") { backStackEntry ->
            val productName = backStackEntry.arguments?.getString("productName") ?: ""
            UpdateQuantityScreen(navController, productName)
        }

    }
}