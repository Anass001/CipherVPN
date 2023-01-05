package com.pixelwave.ciphervpn.network

import com.pixelwave.ciphervpn.model.Server
import retrofit2.Response
import retrofit2.http.GET

interface ServerService {

    @GET("api/iphone/")
    suspend fun getServers(): Response<List<Server>>
}