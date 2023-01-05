package com.pixelwave.ciphervpn.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pixelwave.ciphervpn.model.Server

class ServerSharedViewModel : ViewModel() {
    private val serverMutableLiveData = MutableLiveData<Server>()

    fun select(server: Server) {
        serverMutableLiveData.value = server
    }

    fun getSelected(): MutableLiveData<Server> {
        return serverMutableLiveData
    }
}