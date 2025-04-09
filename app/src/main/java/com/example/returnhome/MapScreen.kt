package com.example.returnhome

import android.Manifest
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.osmdroid.util.GeoPoint

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: RouteViewModel
) {
    val context = LocalContext.current
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Muestra un error si no hay permisos
    if (!permissionsState.allPermissionsGranted) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Button(
                onClick = {
                    permissionsState.launchMultiplePermissionRequest()
                }
            ) {
                Text("Location Permission Required - Tap to Request")
            }
        }
    }

    // Muestra errores del ViewModel
    LaunchedEffect(Unit) {
        viewModel.uiState.collect { state ->
            state.errorMessage?.let { error ->
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            }
        }
    }

    // UI state from ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val homeAddress by viewModel.homeAddress.collectAsState()

    // Dialog states
    var showHomeAddressDialog by remember { mutableStateOf(false) }
    var showCurrentLocation by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("ReturnHome") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FloatingActionButton(
                    onClick = { showCurrentLocation = true },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Icon(Icons.Default.LocationOn, "Current Location")
                }
                FloatingActionButton(
                    onClick = { showHomeAddressDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Default.Home, "Set Home Address")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (permissionsState.allPermissionsGranted) {
                MapWithRoute(
                    currentLocation = uiState.currentLocation,
                    routePoints = uiState.routePoints,
                    homeLocation = uiState.homeLocation,
                    onMapClick = { lat, lon ->
                        viewModel.setHomeLocation(GeoPoint(lat, lon))
                    }
                )

                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.errorMessage?.let { error ->
                    Snackbar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    ) {
                        Text(error)
                    }
                }
            } else {
                LocationPermissionRequest(
                    rationale = "We need your location to show routes to your home",
                    permissionsState = permissionsState
                )
            }

            if (showHomeAddressDialog) {
                HomeAddressDialog(
                    currentAddress = homeAddress,
                    onDismiss = { showHomeAddressDialog = false },
                    onConfirm = { address ->
                        viewModel.setHomeAddress(address)
                        showHomeAddressDialog = false
                    }
                )
            }

            if (showCurrentLocation) {
                CurrentLocationInfoDialog(
                    currentLocation = uiState.currentLocation,
                    onDismiss = { showCurrentLocation = false }
                )
            }
        }
    }

    LaunchedEffect(uiState.homeLocation, uiState.currentLocation) {
        if (uiState.homeLocation != null && uiState.currentLocation != null) {
            viewModel.fetchRoute()
        }
    }
}

@Composable
fun CurrentLocationInfoDialog(
    currentLocation: GeoPoint?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Current Location") },
        text = {
            Column {
                if (currentLocation != null) {
                    Text(text = "Latitude: ${currentLocation.latitude}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Longitude: ${currentLocation.longitude}")
                } else {
                    Text("Location not available")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}