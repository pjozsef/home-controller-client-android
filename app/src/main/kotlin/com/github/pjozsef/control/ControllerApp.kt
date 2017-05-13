package com.github.pjozsef.control

import android.app.Application
import android.content.Intent
import com.github.pjozsef.control.service.UdpService
import com.github.pjozsef.control.service.WebService

class ControllerApp : Application() {

    companion object {
        lateinit var instance: ControllerApp
            private set
    }

    override fun onCreate() {
        instance = this
        startService(Intent(this, UdpService::class.java))
        startService(Intent(this, WebService::class.java))
    }
}