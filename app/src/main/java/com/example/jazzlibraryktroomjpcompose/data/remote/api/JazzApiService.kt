package com.example.jazzlibraryktroomjpcompose.data.remote.api

import com.example.jazzlibraryktroomjpcompose.data.remote.models.BootstrapResponse
import com.example.jazzlibraryktroomjpcompose.domain.repository.TypeResponse
import retrofit2.Response
import retrofit2.http.GET

interface JazzApiService {
    @GET("typeService/all")
    suspend fun getApiStatus(): Response<List<TypeResponse>>

    @GET("bootStrapService/all")
    suspend fun getBootstrapData(): Response<BootstrapResponse>
}