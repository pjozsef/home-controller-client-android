package com.github.pjozsef.control

import android.media.RemoteController
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.github.pjozsef.control.controller.HomeController
import com.github.pjozsef.control.discovery.ServiceDiscovery

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "control.MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ServiceDiscovery(MetaData.host, MetaData.port, MetaData.token)
                .discover()
                .firstOrError()
                .flatMap {
                    HomeController(it.ip, it.port).healthCheck()
                }
                .subscribe { response, error ->
                    response?.let {
                        Log.i(TAG, response.toString())
                    } ?: let {
                        Log.e(TAG, "Error happened", error)
                    }
                }
    }
}
