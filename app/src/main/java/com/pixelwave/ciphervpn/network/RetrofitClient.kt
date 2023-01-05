package com.pixelwave.ciphervpn.network

import com.pixelwave.ciphervpn.util.Constants.Companion.BASE_URL
import retrofit2.Retrofit

class RetrofitClient {
    companion object {
//        private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
//            .connectionSpecs(listOf(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT))
//            .build()

        fun getRetrofitInstance(): Retrofit {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
//                .client(okHttpClient)
                .addConverterFactory(ServerResponseBodyConverter())
                .build()
        }
    }

    fun getServerService(): ServerService {
        return getRetrofitInstance().create(ServerService::class.java)
    }
}