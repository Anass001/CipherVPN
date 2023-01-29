package com.pixelwave.ciphervpn.data

import android.content.SharedPreferences
import android.util.Log
import com.pixelwave.ciphervpn.data.db.ServerDao
import com.pixelwave.ciphervpn.data.network.ServerService
import com.pixelwave.ciphervpn.data.model.Server

class Repository(
    private val serverDao: ServerDao,
    private val serverApi: ServerService,
    private val sharedPreferences: SharedPreferences
) {
    private fun shouldUpdate(): Boolean {
        val lastUpdate = sharedPreferences.getLong("last_update_L", 0L)
        return System.currentTimeMillis().minus(lastUpdate) > 900000L
    }

    //TODO: simplify this function
    suspend fun getServers(): List<Server>? {
        try {
            if (shouldUpdate()) {
                val response = serverApi.getServers()
                if (response.isSuccessful) {
                    with(sharedPreferences.edit()) {
                        putLong("last_update_L", System.currentTimeMillis())
                        apply()
                    }
                    return response.body().also { serverDao.updateAll(response.body()!!) }
                } else {
                    println("Servers loading from network failed")
                }
            }
        } catch (e: Exception) {
            println("Servers loading from network failed with exception: ${e.message}")
        }
        return serverDao.getAll()
    }
}