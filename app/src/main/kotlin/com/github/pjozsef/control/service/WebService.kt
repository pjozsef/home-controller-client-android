package com.github.pjozsef.control.service

import android.app.Service
import android.content.Intent
import com.github.pjozsef.control.common.MetaData
import kotlin.concurrent.thread

class WebService : Service() {
    override fun onBind(intent: Intent) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startWebServer()
        return START_STICKY
    }

    fun startWebServer() {
        thread {
            WebServer(this, MetaData.serverPort).start()
        }
    }
}