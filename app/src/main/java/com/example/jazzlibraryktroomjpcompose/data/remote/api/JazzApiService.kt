package com.example.jazzlibraryktroomjpcompose.data.remote.api

import com.example.jazzlibraryktroomjpcompose.data.remote.models.BootstrapResponse
import com.example.jazzlibraryktroomjpcompose.data.remote.models.SyncPage
import com.example.jazzlibraryktroomjpcompose.data.remote.models.SyncResource
import com.example.jazzlibraryktroomjpcompose.data.remote.models.TypeResponse
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface JazzApiService {

    //TODO just create a dedicated endpoint to check the database status -.-
    //this is used for testing if the database is up, i should replace it with helth endpoint response sioon ....
    @GET("typeService/all")
    suspend fun getApiStatus(): Response<List<TypeResponse>>

    @GET("bootStrapService/all")
    suspend fun getBootstrapData(): Response<BootstrapResponse>

    // new primary sync flow
    @GET("sync/manifest")
    suspend fun getSyncManifest(): Response<List<SyncResource>>

    //for pagination
    @GET("sync/{resource}")
    suspend fun getSyncPage(
        @Path("resource") resource: String,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 1000
    ): Response<SyncPage<JsonObject>>
}