package com.pixelwave.ciphervpn.di

import android.content.SharedPreferences
import com.pixelwave.ciphervpn.data.Repository
import com.pixelwave.ciphervpn.data.db.AppDatabase
import com.pixelwave.ciphervpn.data.remote.ServerService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun bindCryptocurrencyRepository(
        serverApi: ServerService,
        serverDb: AppDatabase,
        sharedPreferences: SharedPreferences
    ): Repository {
        return Repository(serverDb, serverApi, sharedPreferences)
    }
}