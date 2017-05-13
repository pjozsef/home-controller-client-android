package com.github.pjozsef.control.main.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.github.pjozsef.control.R
import com.github.pjozsef.control.discovery.ServerInfo
import com.squareup.picasso.Picasso

class ServerListAdapter(val data: List<ServerInfo>, val listener: (ServerInfo) -> Unit) : RecyclerView.Adapter<ServerListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_server, parent, false)
        return ViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount() = data.size

    class ViewHolder(view: View, val listener: (ServerInfo) -> Unit) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.serverName) as TextView
        val ip: TextView = view.findViewById(R.id.serverIp) as TextView
        val icon: ImageView = view.findViewById(R.id.icon) as ImageView

        fun bind(info: ServerInfo) {
            name.text = info.name
            ip.text = info.ip
            val iconId = when{
                info.os.contains("Mac") -> R.drawable.logo_mac
                info.os.contains("Linux") -> R.drawable.logo_linux
                info.os.contains("Windows") -> R.drawable.logo_windows
                else -> R.drawable.logo_unknown
            }
            Picasso.with(itemView.context).load(iconId).into(icon)
            icon.setOnClickListener { listener(info) }
        }
    }
}