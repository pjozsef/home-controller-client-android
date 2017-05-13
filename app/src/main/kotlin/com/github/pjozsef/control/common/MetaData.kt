package com.github.pjozsef.control.common

import android.content.pm.PackageManager
import android.util.Log
import com.github.pjozsef.control.ControllerApp

object MetaData {
    val TAG = "MetaData"

    private val map : Map<String, Any> by lazy {
        with(ControllerApp.instance){
            val metadata = packageManager
                    .getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                    .metaData
            metadata.keySet().associate { it to metadata[it] }
        }
    }

    val host: String by map
    val port: Int by map
    val udpToken: String by map
    val httpToken: String by map
    val serverPort: Int by map
}