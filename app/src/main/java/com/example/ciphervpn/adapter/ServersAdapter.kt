package com.example.ciphervpn.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ciphervpn.R
import com.example.ciphervpn.data.model.Server

class ServersAdapter(private val servers: List<Server>) :
    RecyclerView.Adapter<ServersAdapter.ViewHolder>() {

    private var itemClickListener: ItemClickListener? = null

    interface ItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val serverName: TextView = itemView.findViewById(R.id.server_name_tv)
        val serverIp: TextView = itemView.findViewById(R.id.server_ip_tv)

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val view = inflater.inflate(R.layout.servers_rv_item, parent, false)
                return ViewHolder(view)
            }
        }

        fun bind(server: Server, itemClickListener: ItemClickListener?) {
            serverName.text = server.hostName
            serverIp.text = server.ipAddress
            itemView.setOnClickListener {
                itemClickListener?.onItemClick(it, adapterPosition)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val server = servers[position]
        holder.bind(server, itemClickListener)
    }

    override fun getItemCount(): Int {
        return servers.size
    }
}