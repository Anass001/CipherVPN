package com.pixelwave.ciphervpn.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.pixelwave.ciphervpn.data.db.AppDatabase
import com.pixelwave.ciphervpn.data.db.DatabaseBuilder
import com.pixelwave.ciphervpn.data.remote.RetrofitClient
import com.pixelwave.ciphervpn.data.remote.ServerService
import com.pixelwave.ciphervpn.util.Constants.Companion.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideServerApi(): ServerService {
        return RetrofitClient().getServerService()
    }

    @Provides
    @Singleton
    fun providesServerDatabase(app: Application): AppDatabase {
        return DatabaseBuilder.getInstance(app)
    }

    @Provides
    @Singleton
    fun providesSharedPreferences(app: Application): SharedPreferences =
        app.getSharedPreferences("timestamps", Context.MODE_PRIVATE)

}