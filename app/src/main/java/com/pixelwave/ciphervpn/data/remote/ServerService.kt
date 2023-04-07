package com.pixelwave.ciphervpn.data.remote

import com.pixelwave.ciphervpn.data.model.Server
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers

interface ServerService {
    @GET("api/iphone/")
//    @Headers(
//        "Accept: text/plain"
//    )
    suspend fun getServers(
    ): Response<List<Server>>
}