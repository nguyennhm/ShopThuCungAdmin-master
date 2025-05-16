package com.example.shopthucungAdmin_master.admin.navigation

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.shopthucungAdmin_master.admin.view.*
import com.example.shopthucungAdmin_master.admin.viewmodel.LoginViewModel
import com.example.shopthucungAdmin_master.admin.viewmodel.RegisterViewModel
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
        composable("main") {
            MainScreen(navController = navController) // Sử dụng MainScreen thay vì AdminDashboardScreen
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
        composable(
            route = "order_detail/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            OrderDetailScreen(orderId = orderId, navController = navController)
        }
        composable(
            route = "update_quantity/{productName}",
            arguments = listOf(navArgument("productName") { type = NavType.StringType })
        ) { backStackEntry ->
            val productName = backStackEntry.arguments?.getString("productName") ?: ""
            UpdateQuantityScreen(navController, productName)
        }
    }
}