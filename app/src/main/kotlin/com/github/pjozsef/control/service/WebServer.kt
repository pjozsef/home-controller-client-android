package com.github.pjozsef.control.service

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.AudioManager.*
import android.util.Log
import android.view.KeyEvent
import com.github.pjozsef.control.R
import com.github.pjozsef.control.common.MetaData
import com.github.pjozsef.control.controller.model.request.Volume
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import fi.iki.elonen.NanoHTTPD

class WebServer(val context: Context, port: Int) : NanoHTTPD(port) {
    val TAG = "webserver"
    val gson = Gson()

    override fun serve(session: IHTTPSession): Response {
        Log.i(TAG, session.headers.toString())
        Log.i(TAG, session.uri)
        return requireAuth(session) {
            when (session.uri) {
                "/command/supported" -> handleSupported()
                "/command/playpause" -> handleMusic(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
                "/command/next" -> handleMusic(KeyEvent.KEYCODE_MEDIA_NEXT)
                "/command/prev" -> handleMusic(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
                "/command/mute" -> handleMute()
                "/command/volup" -> handleVolUp()
                "/command/voldown" -> handleVolDown()
                "/command/volume" -> {
                    val map = HashMap<String, String>()
                    session.parseBody(map)
                    try {
                        val volume = gson.fromJson(map["postData"], Volume::class.java)
                        handleVolume(volume.level)
                    } catch (jse: JsonSyntaxException){
                        badRequestResponse("Cannot determine volume level!")
                    }
                }
                else -> handleUnsupported()
            }
        }
    }

    private inline fun requireAuth(session: IHTTPSession, action: () -> Response): Response {
        if (session.headers["token"] == MetaData.httpToken) {
            return action()
        } else {
            return newFixedLengthResponse(Response.Status.UNAUTHORIZED, NanoHTTPD.MIME_PLAINTEXT, "")
        }
    }

    private fun handleSupported() = newFixedLengthResponse(
            Response.Status.OK,
            NanoHTTPD.MIME_PLAINTEXT,
            "{\"commands\": [\"playpause\",\"next\",\"prev\",\"mute\",\"volup\",\"voldown\",\"setvolume\"]}")

    private fun handleMusic(key: Int): NanoHTTPD.Response {
        val intent = Intent(Intent.ACTION_MEDIA_BUTTON)

        intent.putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_DOWN, key))
        context.sendOrderedBroadcast(intent, null)

        intent.putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_UP, key))
        context.sendOrderedBroadcast(intent, null)

        return okResponse()
    }

    private fun handleMute(): NanoHTTPD.Response = handleWithAudioManager {
        val volume = getStreamVolume(STREAM_MUSIC)
        val sharedPrefs = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)

        if (volume > 0) {
            setStreamVolume(STREAM_MUSIC, 0, 0)
            sharedPrefs.edit()
                    .putInt("volume", volume)
                    .apply()
        } else {
            val previousVolume = sharedPrefs.getInt("volume", 0)
            setStreamVolume(STREAM_MUSIC, previousVolume, 0)
        }
    }

    private fun handleVolUp(): NanoHTTPD.Response = handleWithAudioManager {
        adjustStreamVolume(STREAM_MUSIC, ADJUST_RAISE, 0)
    }

    private fun handleVolDown(): NanoHTTPD.Response = handleWithAudioManager {
        adjustStreamVolume(STREAM_MUSIC, ADJUST_LOWER, 0)
    }

    private fun handleVolume(level: Int): NanoHTTPD.Response = handleWithAudioManager {
        setStreamVolume(STREAM_MUSIC, normalize(level), 0)
    }

    private fun normalize(level: Int) =
            if (level < 0) 0
            else if (level > 100) 100
            else level


    fun handleWithAudioManager(action: AudioManager.() -> Unit): NanoHTTPD.Response {
        (context.getSystemService(Context.AUDIO_SERVICE) as AudioManager).action()
        return okResponse()
    }

    private fun handleUnsupported() = newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "")

    private fun okResponse() = newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, "{\"result\": \"success\"}")

    private fun badRequestResponse(cause: String) = newFixedLengthResponse(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "{\"result\": \"failure\", \"cause\": \"$cause\"}")

}