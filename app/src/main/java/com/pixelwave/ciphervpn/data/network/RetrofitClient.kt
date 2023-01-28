package com.pixelwave.ciphervpn.data.network

import com.pixelwave.ciphervpn.util.Constants.Companion.BASE_URL
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class RetrofitClient {
    companion object {

        private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .readTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .build()

        fun getRetrofitInstance(): Retrofit {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ServerResponseBodyConverter())
                .client(okHttpClient)
                .build()
        }
    }

    fun getServerService(): ServerService {
        return getRetrofitInstance().create(ServerService::class.java)
    }
}