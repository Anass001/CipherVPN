package com.pixelwave.ciphervpn.data.network

import com.pixelwave.ciphervpn.data.model.Server
import retrofit2.Response
import retrofit2.http.GET

interface ServerService {

    @GET("api/iphone/")
    suspend fun getServers(): Response<List<Server>>
}