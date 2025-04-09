package com.example.returnhome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.returnhome.util.LocationClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class RouteViewModel(
    private val locationClient: LocationClient,
    private val api: OpenRouteServiceApi
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _homeAddress = MutableStateFlow("")
    val homeAddress: StateFlow<String> = _homeAddress.asStateFlow()

    init {
        getCurrentLocation()
    }

    fun getCurrentLocation() {
        viewModelScope.launch {
            try {
                locationClient
                    .getLocationUpdates()
                    .catch { e ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Location error: ${e.message}"
                        )
                    }
                    .collect { location ->
                        _uiState.value = _uiState.value.copy(
                            currentLocation = GeoPoint(
                                location.latitude,
                                location.longitude
                            ),
                            errorMessage = null
                        )
                    }
            } catch (e: SecurityException) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Location permission required"
                )
            }
        }
    }

    fun setHomeAddress(address: String) {
        _homeAddress.value = address
        viewModelScope.launch {
            val context = locationClient.getApplicationContext()
            val geoPoint = geocodeAddress(context, address)
            geoPoint?.let {
                _uiState.value = _uiState.value.copy(
                    homeLocation = it
                )
            }
        }
    }

    fun setHomeLocation(geoPoint: GeoPoint) {
        _uiState.value = _uiState.value.copy(
            homeLocation = geoPoint
        )
    }

    fun fetchRoute() {
        val current = _uiState.value.currentLocation ?: return
        val home = _uiState.value.homeLocation ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val response = api.getRoute(
                    startLon = current.longitude,
                    startLat = current.latitude,
                    endLon = home.longitude,
                    endLat = home.latitude
                )

                val routePoints = response.features.firstOrNull()?.geometry?.coordinates
                    ?.map { GeoPoint(it[1], it[0]) } ?: emptyList()

                _uiState.value = _uiState.value.copy(
                    routePoints = routePoints,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to fetch route: ${e.message}"
                )
            }
        }
    }
}

class RouteViewModelFactory(
    private val locationClient: LocationClient,
    private val api: OpenRouteServiceApi
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RouteViewModel::class.java)) {
            return RouteViewModel(locationClient, api) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}