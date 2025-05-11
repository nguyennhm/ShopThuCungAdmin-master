package com.example.shopthucungAdmin_master

import android.app.Application
import com.cloudinary.android.MediaManager

class MyApp : Application() {

    companion object {
        var isMediaManagerInitialized = false
    }

    override fun onCreate() {
        super.onCreate()

        if (!isMediaManagerInitialized) {
            val config: HashMap<String, String> = HashMap()
            config["cloud_name"] = "ten-cloud-cua-ban"
            config["api_key"] = "apikey"
            config["api_secret"] = "apisecret"
            MediaManager.init(this, config)
            isMediaManagerInitialized = true
        }
    }
}
