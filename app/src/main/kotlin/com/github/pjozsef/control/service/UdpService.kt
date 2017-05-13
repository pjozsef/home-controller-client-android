package com.github.pjozsef.control.service

import android.app.Service
import android.content.Intent
import android.os.Build
import android.util.Log
import com.github.pjozsef.control.common.MetaData
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.NetworkInterface
import kotlin.concurrent.thread


class UdpService : Service() {

    val TAG = "webserver"

    override fun onBind(intent: Intent) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startUdpServer()
        return START_STICKY
    }

    private fun startUdpServer() {
        thread(isDaemon = true) {
            val socket = DatagramSocket(MetaData.port)
            while (true) {
                val buf = ByteArray(512)
                val packet = DatagramPacket(buf, buf.size)
                socket.receive(packet)
                if (packet.message() == MetaData.udpToken) {
                    val reply = createReply().toByteArray()
                    socket.send(DatagramPacket(reply, reply.size, packet.address, packet.port))
                }
            }
        }
    }

    private fun DatagramPacket.message(): String {
        val index = this.data.mapIndexed { index, byte ->
            index to byte
        }.filter {
            val zero: Byte = 0
            it.second == zero
        }.map {
            it.first
        }.first() - 1
        return String(this.data.sliceArray(0..index))
    }

    private fun createReply() =
            "${Build.MODEL},${getLocalIp()},${Build.VERSION.RELEASE},${MetaData.serverPort}"

    private fun getLocalIp(): String {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val addresses = interfaces.nextElement().inetAddresses
            while (addresses.hasMoreElements()) {
                val address = addresses.nextElement().hostAddress
                if (address.startsWith("192.168.")) {
                    return address
                }
            }
        }
        return "Unable to determine local ip address!"
    }
}