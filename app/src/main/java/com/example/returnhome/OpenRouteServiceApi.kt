package com.example.returnhome

import com.example.returnhome.*
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenRouteServiceApi {
    @GET("v2/directions/driving-car")
    suspend fun getRoute(
        @Query("api_key") apiKey: String = "5b3ce3597851110001cf62483cfe0e5dda884fb6ba9a6dbe36cbd7de",
        @Query("start") start: String,
        @Query("end") end: String
    ): RouteResponse
}

// Helper extension function for cleaner calls
suspend fun OpenRouteServiceApi.getRoute(
    startLon: Double,
    startLat: Double,
    endLon: Double,
    endLat: Double
): RouteResponse {
    return getRoute(
        start = "$startLon,$startLat",
        end = "$endLon,$endLat"
    )
}