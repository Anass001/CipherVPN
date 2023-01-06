package com.pixelwave.ciphervpn.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.pixelwave.ciphervpn.data.model.Server

@Dao
interface ServerDao {

    @Query("SELECT * FROM server")
    suspend fun getAll(): List<Server>

    @Insert
    suspend fun insertAll(vararg servers: Server)

    @Query("DELETE FROM server")
    suspend fun deleteAll()

    @Transaction
    suspend fun updateAll(servers: List<Server>) {
        deleteAll()
        insertAll(*servers.toTypedArray())
    }
}