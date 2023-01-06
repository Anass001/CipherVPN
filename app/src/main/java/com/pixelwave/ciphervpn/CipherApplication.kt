package com.pixelwave.ciphervpn

import android.app.Application
import com.pixelwave.ciphervpn.data.Repository
import com.pixelwave.ciphervpn.data.db.DatabaseBuilder
import com.pixelwave.ciphervpn.data.network.RetrofitClient

class CipherApplication : Application() {
    private val database by lazy { DatabaseBuilder.getInstance(this) }
    private val apiClient by lazy { RetrofitClient() }
    val repository by lazy { Repository(database.serverDao(), apiClient.getServerService()) }
}