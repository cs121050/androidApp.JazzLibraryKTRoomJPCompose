package com.example.jazzlibraryktroomjpcompose.data.remote.api

import com.example.jazzlibraryktroomjpcompose.data.remote.models.BootstrapResponse
import com.example.jazzlibraryktroomjpcompose.data.remote.models.TypeResponse
import retrofit2.Response
import retrofit2.http.GET

interface JazzApiService {

    //TODO just create a dedicated endpoint to check the database status -.-
    //this is used for testing if the database is up, i should replace it with helth endpoint response sioon ....
    @GET("typeService/all")
    suspend fun getApiStatus(): Response<List<TypeResponse>>

    @GET("bootStrapService/all")
    suspend fun getBootstrapData(): Response<BootstrapResponse>
}