package com.pixelwave.ciphervpn.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pixelwave.ciphervpn.data.model.Server

@Database(entities = [Server::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serverDao(): ServerDao
}