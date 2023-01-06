package com.pixelwave.ciphervpn.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelwave.ciphervpn.data.Repository
import com.pixelwave.ciphervpn.data.db.AppDatabase
import com.pixelwave.ciphervpn.data.db.DatabaseBuilder
import com.pixelwave.ciphervpn.data.model.Server
import com.pixelwave.ciphervpn.data.network.RetrofitClient
import com.pixelwave.ciphervpn.util.Constants
import com.pixelwave.ciphervpn.util.CsvParser
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.ResponseBody

class ServersViewModel(private val repository: Repository) : ViewModel() {

    private var servers: MutableLiveData<List<Server>> = MutableLiveData()

    private fun loadServersList() {
        viewModelScope.launch {
            val serversList = repository.servers()
            serversList?.let {
                servers.postValue(it)
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