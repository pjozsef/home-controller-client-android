package com.github.pjozsef.control.control

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.github.pjozsef.control.R
import com.github.pjozsef.control.common.MetaData
import com.github.pjozsef.control.controller.HomeController
import com.github.pjozsef.control.controller.model.request.Volume
import com.github.pjozsef.control.discovery.ServerInfo
import com.google.gson.Gson
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_controller.*

class ControllerActivity : AppCompatActivity() {

    val TAG = "controllerActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_controller)

        val info = Gson().fromJson(intent.getStringExtra("info"), ServerInfo::class.java)
        val controller = HomeController(info.ip, info.port, MetaData.httpToken)

        shutdown.setOnClickListener {
            controller.shutdown()
        }

        playPause.setOnClickListener {
            controller.playPause()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { response, error ->
                        Log.i(TAG, response.toString())
                        error?.let {
                            Log.e(TAG, error.toString())
                        }
                    }
        }

        shutdown.setOnClickListener(apiCall(controller::shutdown))
        restart.setOnClickListener(apiCall(controller::restart))
        suspend.setOnClickListener(apiCall(controller::suspend))

        playPause.setOnClickListener(apiCall(controller::playPause))
        next.setOnClickListener(apiCall(controller::next))
        prev.setOnClickListener(apiCall(controller::previous))

        mute.setOnClickListener(apiCall(controller::mute))
        volUp.setOnClickListener(apiCall(controller::volUp))
        volDown.setOnClickListener(apiCall(controller::volDown))
        volume.setOnClickListener(apiCall{ controller.setVolume(Volume(volBar.progress)) })
    }

    fun <T> apiCall(callFactory: () -> Single<T>): (View) -> Unit {
        return { view ->
            callFactory()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { response, error ->
                        Log.i(TAG, response.toString())
                        error?.let {
                            Log.e(TAG, error.toString())
                        }
                    }
        }
    }
}