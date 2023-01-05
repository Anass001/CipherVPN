package com.pixelwave.ciphervpn.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelwave.ciphervpn.model.Server
import com.pixelwave.ciphervpn.network.RetrofitClient
import kotlinx.coroutines.launch

class ServersViewModel : ViewModel() {

    private val retrofitClient = RetrofitClient()
    private val serverService = retrofitClient.getServerService()
    private var servers: MutableLiveData<List<Server>> = MutableLiveData()
    
    private fun loadServersList() {
        viewModelScope.launch {
            try {
                val response = serverService.getServers()
                if (response.isSuccessful) {
                    servers.value = response.body()
                } else {
                    println("Servers loading failed")
                }
            } catch (e: Exception) {
                println("Servers loading failed with exception: ${e.message}")
            }
        }
    }

    fun getServers(): LiveData<List<Server>> {
        if (servers.value == null) {
            loadServersList()
        }
        return servers
    }
}