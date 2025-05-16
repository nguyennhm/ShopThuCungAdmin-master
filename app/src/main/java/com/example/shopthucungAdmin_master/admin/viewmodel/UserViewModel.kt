package com.example.shopthucungAdmin_master.admin.viewmodel

import android.content.Context
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.ViewModel
import com.example.shopthucungAdmin_master.model.User
import com.example.shopthucungAdmin_master.utils.WebAppInterface
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.*

class UserViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users
    private val client = OkHttpClient()

    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        db.collection("user").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val userList = snapshot.documents.mapNotNull { it.toObject(User::class.java) }
                _users.value = userList
            }
        }
    }

    fun updateUserActiveStatus(
        userId: String,
        newStatus: Boolean,
        context: Context
    ) {
        val user = _users.value.find { it.idUser == userId }

        if (user != null) {
            val email = user.email ?: return
            val hoVaTen = user.hoVaTen ?: "Người dùng"  // Giả sử bạn có trường này trong model User

            db.collection("user").document(userId)
                .update("active", newStatus)
                .addOnSuccessListener {
                    sendEmailWithWebView(context, email, newStatus, hoVaTen)
                }
                .addOnFailureListener { exception ->
                    println("❌ Cập nhật trạng thái thất bại: ${exception.message}")
                }
        } else {
            println("❌ Không tìm thấy user với ID: $userId")
        }
    }



    fun sendEmailWithWebView(context: Context, email: String, isActive: Boolean, hoVaTen: String) {
        val status = if (isActive) "Mở" else "Khóa"
        val safeEmail = email.replace("'", "\\'")
        val safeStatus = status.replace("'", "\\'")
        val safeHoVaTen = hoVaTen.replace("'", "\\'")

        val webView = WebView(context).apply {
            settings.javaScriptEnabled = true

            addJavascriptInterface(WebAppInterface(context), "Android")

            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                    Log.d("WEBVIEW_CONSOLE", "${consoleMessage.message()} -- from line ${consoleMessage.lineNumber()}")
                    return true
                }
            }

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    val jsCode = "sendEmail('$safeEmail', '$safeStatus', '$safeHoVaTen');"
                    Log.d("WEBVIEW_JS", "🧪 Gọi JS: $jsCode")
                    view?.evaluateJavascript(jsCode, null)
                }
            }
        }
        webView.loadUrl("file:///android_asset/email_sender.html")
    }


}
