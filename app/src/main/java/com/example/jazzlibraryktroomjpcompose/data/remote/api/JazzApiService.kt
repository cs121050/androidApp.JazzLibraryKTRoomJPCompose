package com.example.jazzlibraryktroomjpcompose.data.remote.api

import com.example.jazzlibraryktroomjpcompose.data.remote.models.BootstrapResponse
import retrofit2.Response
import retrofit2.http.GET

interface JazzApiService {
    @GET("bootStrapService/all")
    suspend fun getBootstrapData(): Response<BootstrapResponse>
}