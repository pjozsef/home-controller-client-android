package com.github.pjozsef.control

import android.content.pm.PackageManager
import android.util.Log

object MetaData {
    val TAG = "MetaData"
    private val map = HashMap<String, Any>()
    val host: String by map
    val port: Int by map
    val token: String by map
    init {
        with(ControllerApp.instance){
            val bundle = packageManager
                    .getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                    .metaData
            bundle.keySet().forEach { key ->
                map[key] = bundle[key]
            }
        }
    }
}