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
        val lastUpdate = sharedPreferences.getInt("last_update", 0)
        return System.currentTimeMillis() - lastUpdate > 900000
    }

    suspend fun getServers(): List<Server>? {
        return try {
            if (shouldUpdate()) {
                val response = serverApi.getServers()
                if (response.isSuccessful) {
                    with(sharedPreferences.edit()) {
                        putInt("last_update", System.currentTimeMillis().toInt())
                        apply()
                    }
                    Log.i("Repository", sharedPreferences.getInt("last_update", 0).toString())
                    response.body().also { serverDao.updateAll(response.body()!!) }
                } else {
                    println("Servers loading from network failed")
                    serverDao.getAll()
                }
            } else {
                serverDao.getAll()
            }
        } catch (e: Exception) {
            println("Servers loading from network failed with exception: ${e.message}")
            serverDao.getAll()
        }
    }
}