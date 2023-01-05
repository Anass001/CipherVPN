package com.pixelwave.ciphervpn.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pixelwave.ciphervpn.R
import com.pixelwave.ciphervpn.model.Server

class ServersAdapter :
    RecyclerView.Adapter<ServersAdapter.ViewHolder>() {

    private var itemClickListener: ItemClickListener? = null
    private var servers: MutableList<Server> = mutableListOf()

    interface ItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    fun setOnItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    fun setData(servers: MutableList<Server>) {
        this.servers = servers
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val serverName: TextView = itemView.findViewById(com.pixelwave.ciphervpn.R.id.server_name_tv)
        private val serverIp: TextView = itemView.findViewById(R.id.server_ip_tv)

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