package com.pixelwave.ciphervpn.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelwave.ciphervpn.data.Repository
import com.pixelwave.ciphervpn.data.model.Server
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServersViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    private var servers: MutableLiveData<List<Server>> = MutableLiveData()

    private fun loadServersList() {
        viewModelScope.launch {
            val serversList = repository.getServers()
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

    fun refreshServers() {
        loadServersList()
    }
}