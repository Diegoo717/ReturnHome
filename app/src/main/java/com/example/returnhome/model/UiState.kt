package com.example.returnhome.model

import org.osmdroid.util.GeoPoint

data class UiState(
    val currentLocation: GeoPoint? = null,
    val homeLocation: GeoPoint? = null,
    val routePoints: List<GeoPoint> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)