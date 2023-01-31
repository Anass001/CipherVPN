package com.pixelwave.ciphervpn.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.murgupluoglu.flagkit.FlagKit
import com.pixelwave.ciphervpn.R
import com.pixelwave.ciphervpn.data.model.ConnectionStatus
import com.pixelwave.ciphervpn.data.model.Server
import com.pixelwave.ciphervpn.util.RippleBackground
import com.pixelwave.ciphervpn.viewmodel.ServerSharedViewModel
import de.hdodenhof.circleimageview.CircleImageView

class ServersAdapter(private val serverSharedViewModel: ServerSharedViewModel) :
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

        private val serverImage: CircleImageView = itemView.findViewById(R.id.server_image)
        private val serverName: TextView =
            itemView.findViewById(R.id.server_name_tv)
        private val serverPing: TextView = itemView.findViewById(R.id.server_ping_tv)
        private val serverStatus: CircleImageView =
            itemView.findViewById(R.id.server_status)
        val connectionStatus: TextView = itemView.findViewById(R.id.connection_status_tv)

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val view = inflater.inflate(R.layout.servers_rv_item, parent, false)
                return ViewHolder(view)
            }
        }

        fun bind(server: Server, itemClickListener: ItemClickListener?) {
            serverName.text = server.countryLong
            serverPing.text = itemView.context.resources.getString(R.string.ping_ms, server.ping)

            when (server.ping.toInt()) {
                in 0..50 -> serverStatus.setImageResource(R.color.green)
                in 51..100 -> serverStatus.setImageResource(R.color.orange)
                else -> serverStatus.setImageResource(R.color.red)
            }

            Glide.with(itemView.context)
                .load(FlagKit.getDrawable(itemView.context, server.countryShort))
                .into(serverImage)

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

        val currServer = serverSharedViewModel.getSelected().value
        val isConnected =
            serverSharedViewModel.getConnectionStatus().value == ConnectionStatus.CONNECTED
        if (isConnected && currServer?.id == server.id) {
            holder.connectionStatus.visibility = View.VISIBLE
        } else {
            holder.connectionStatus.visibility = View.GONE
        }

        holder.bind(server, itemClickListener)
    }

    override fun getItemCount(): Int {
        return servers.size
    }
}