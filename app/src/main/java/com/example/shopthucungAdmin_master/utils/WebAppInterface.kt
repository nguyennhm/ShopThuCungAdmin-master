package com.example.shopthucungAdmin_master.utils

import android.content.Context
import android.webkit.JavascriptInterface
import android.widget.Toast

class WebAppInterface(private val context: Context) {
    @JavascriptInterface
    fun onEmailResult(result: String) {
        // Kết quả gửi email từ JS
        if (result == "success") {
            Toast.makeText(context, "Email đã được gửi thành công!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Gửi email thất bại!", Toast.LENGTH_SHORT).show()
        }
    }
}
