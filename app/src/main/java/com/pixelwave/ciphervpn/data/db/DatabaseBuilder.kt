package com.pixelwave.ciphervpn.data.db

import android.content.Context
import androidx.room.Room

object DatabaseBuilder {
    private var INSTANCE: AppDatabase? = null

    fun getInstance(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = build(context)
            INSTANCE = instance
            instance
        }
    }

    private fun build(context: Context) = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java, "server-db-0"
    ).build()
}