package com.pixelwave.ciphervpn.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Server(
    val hostName: String = "",
    val ipAddress: String = "",
    val score: Int = 0,
    val ping: String = "",
    val speed: Long = 0,
    val countryLong: String = "",
    val countryShort: String = "",
    val vpnSessions: Long = 0,
    val uptime: Long = 0,
    val totalUsers: Long = 0,
    val totalTraffic: String = "",
    val logType: String = "",
    val operator: String = "",
    val message: String = "",
    val openVpnConfigData: String = "",
    val port: Int = 0,
    val protocol: String = "",
    val isStarred: Boolean = false
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}