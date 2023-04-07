package com.pixelwave.ciphervpn.data

import android.content.SharedPreferences
import com.pixelwave.ciphervpn.data.db.AppDatabase
import com.pixelwave.ciphervpn.data.db.ServerDao
import com.pixelwave.ciphervpn.data.remote.ServerService
import com.pixelwave.ciphervpn.data.model.Server
import retrofit2.Retrofit
import javax.inject.Inject

class Repository @Inject constructor(
    private val serverDb: AppDatabase,
    private val serverApi: ServerService,
    private val sharedPreferences: SharedPreferences
) {
    private val serverDao: ServerDao = serverDb.serverDao()
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
            println("Servers loading from network failed with exception: ${e.localizedMessage}")
        }
        return serverDao.getAll()
    }
}