package com.github.pjozsef.control.main

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.github.pjozsef.control.R
import com.github.pjozsef.control.common.MetaData
import com.github.pjozsef.control.control.ControllerActivity
import com.github.pjozsef.control.discovery.ServerInfo
import com.github.pjozsef.control.discovery.ServiceDiscovery
import com.github.pjozsef.control.main.adapter.ServerListAdapter
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    val TAG = "control.MainActivity"

    val discovery: ServiceDiscovery
    val serversList: MutableList<ServerInfo>
    val adapter: ServerListAdapter

    init {
        discovery = ServiceDiscovery(MetaData.host, MetaData.port, MetaData.udpToken)
        serversList = mutableListOf<ServerInfo>()
        adapter = ServerListAdapter(serversList) { info ->
            val intent = Intent(this, ControllerActivity::class.java).apply {
                val gson = Gson()
                putExtra("info", gson.toJson(info))
            }
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_refresh -> loadData()
            else -> {
            }
        }
        return true
    }

    private fun setupRecyclerView() {
        serverList.layoutManager = LinearLayoutManager(this)
        serverList.adapter = adapter
    }

    private fun loadData() {
        discovery.discover()
                .buffer(200, TimeUnit.MILLISECONDS)
                .scan { first, second -> first + second  }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    serversList.clear()
                    serversList += response
                    adapter.notifyDataSetChanged()
                }, { error ->
                    Log.e(TAG, "Error happened", error)
                }, {
                    Log.i(TAG, serversList.toString())
                })
    }
}
