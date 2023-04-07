package com.pixelwave.ciphervpn.util

import android.util.Base64
import com.opencsv.CSVReader
import com.pixelwave.ciphervpn.data.model.Server
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.time.LocalDate

object CsvParser {
    private const val HOST_NAME = 0
    private const val IP_ADDRESS = 1
    private const val SCORE = 2
    private const val PING = 3
    private const val SPEED = 4
    private const val COUNTRY_LONG = 5
    private const val COUNTRY_SHORT = 6
    private const val VPN_SESSION = 7
    private const val UPTIME = 8
    private const val TOTAL_USERS = 9
    private const val TOTAL_TRAFFIC = 10
    private const val LOG_TYPE = 11
    private const val OPERATOR = 12
    private const val MESSAGE = 13
    private const val OVPN_CONFIG_DATA = 14
    private const val PORT_INDEX = 2
    private const val PROTOCOL_INDEX = 1

    fun stringToServer(vpn: Array<String>): Server {
//        val vpn = line.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return Server(
            vpn[HOST_NAME],
            vpn[IP_ADDRESS],
            vpn[SCORE].toInt(),
            vpn[PING],
            vpn[SPEED].toLong(),
            vpn[COUNTRY_LONG],
            vpn[COUNTRY_SHORT],
            vpn[VPN_SESSION].toLong(),
            vpn[UPTIME].toLong(),
            vpn[TOTAL_USERS].toLong(),
            vpn[TOTAL_TRAFFIC],
            vpn[LOG_TYPE],
            vpn[OPERATOR],
            vpn[MESSAGE],
            String(Base64.decode(vpn[OVPN_CONFIG_DATA], Base64.DEFAULT)),
            getPort(
                vpn[OVPN_CONFIG_DATA].split("[\\r\\n]+".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()),
            getProtocol(
                vpn[OVPN_CONFIG_DATA].split("[\\r\\n]+".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()),
            false)
    }

    fun parse(stream: InputStream): List<Server> {
        val csvReader = CSVReader(InputStreamReader(stream))
        return csvReader.readAll()
            .drop(2)
            .dropLast(1)
            .dropLastWhile { it.isEmpty() }
            .mapNotNull { line ->
                stringToServer(line)
            }
            .also { csvReader.close() }
    }

    /**
     * @return Port used in OVPN file ("remote <HOSTNAME> <PORT>")
    </PORT></HOSTNAME> */
    private fun getPort(lines: Array<String>): Int {
        var port = 0
        for (line in lines) {
            if (!line.startsWith("#")) {
                if (line.startsWith("remote")) {
                    port = line.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()[PORT_INDEX].toInt()
                    break
                }
            }
        }
        return port
    }

    /**
     * @return Protocol used in OVPN file. ("proto <TCP></TCP>/UDP>")
     */
    private fun getProtocol(lines: Array<String>): String {
        var protocol = ""
        for (line in lines) {
            if (!line.startsWith("#")) {
                if (line.startsWith("proto")) {
                    protocol = line.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()[PROTOCOL_INDEX]
                    break
                }
            }
        }
        return protocol
    }
}