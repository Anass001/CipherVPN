package com.example.ciphervpn.data.model

class Server(
    val hostName: String,
    val ipAddress: String,
    val score: Int,
    val ping: String,
    val speed: Long,
    val countryLong: String,
    val countryShort: String,
    val vpnSessions: Long,
    val uptime: Long,
    val totalUsers: Long,
    val totalTraffic: String,
    val logType: String,
    val operator: String,
    val message: String,
    val ovpnConfigData: String,
    val port: Int,
    val protocol: String,
    val isStarred: Boolean
)