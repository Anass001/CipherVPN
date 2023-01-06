package com.pixelwave.ciphervpn.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pixelwave.ciphervpn.data.model.ConnectionStatus
import com.pixelwave.ciphervpn.data.model.Server

class ServerSharedViewModel : ViewModel() {
    private val serverMutableLiveData = MutableLiveData<Server>()
    private val connectionStatusMutableLiveData = MutableLiveData<ConnectionStatus>()

    init {
        connectionStatusMutableLiveData.value = ConnectionStatus.DISCONNECTED
    }

    fun select(server: Server) {
        serverMutableLiveData.value = server
    }

    fun getSelected(): MutableLiveData<Server> {
        return serverMutableLiveData
    }

    fun updateConnectionStatus(status: ConnectionStatus) {
        connectionStatusMutableLiveData.value = status
    }

    fun getConnectionStatus(): MutableLiveData<ConnectionStatus> {
        return connectionStatusMutableLiveData
    }
}