package com.github.pjozsef.control

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
        ServiceDiscovery("192.168.0.255", 7788, "secretToken1")
                .discover()
                .firstOrError()
                .flatMap {
                    HomeController(it.ip, it.port).healthCheck()
                }
                .subscribe { response, error ->
                    Log.i(TAG, response.toString())
                }
    }
}
