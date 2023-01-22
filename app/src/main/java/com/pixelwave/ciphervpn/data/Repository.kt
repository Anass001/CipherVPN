package com.pixelwave.ciphervpn.data

import com.pixelwave.ciphervpn.data.db.ServerDao
import com.pixelwave.ciphervpn.data.network.ServerService
import com.pixelwave.ciphervpn.data.model.Server

class Repository(private val serverDao: ServerDao, private val serverApi: ServerService) {

    suspend fun getServers(): List<Server>? {
        return try {
            val response = serverApi.getServers()
            if (response.isSuccessful) {
                response.body().also { serverDao.updateAll(response.body()!!) }
            } else {
                println("Servers loading from network failed")
                serverDao.getAll()
            }
        } catch (e: Exception) {
            println("Servers loading from network failed with exception: ${e.message}")
            serverDao.getAll()
        }
    }
}