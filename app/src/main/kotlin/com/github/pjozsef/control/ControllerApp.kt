package com.github.pjozsef.control

import android.app.Application

class ControllerApp : Application() {

    companion object {
        lateinit var instance: ControllerApp
            private set
    }

    override fun onCreate() {
        instance = this
    }
}