package com.pixelwave.ciphervpn.data.network

import com.pixelwave.ciphervpn.util.Constants.Companion.BASE_URL
import retrofit2.Retrofit

class RetrofitClient {
    companion object {
        fun getRetrofitInstance(): Retrofit {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ServerResponseBodyConverter())
                .build()
        }
    }

    fun getServerService(): ServerService {
        return getRetrofitInstance().create(ServerService::class.java)
    }
}