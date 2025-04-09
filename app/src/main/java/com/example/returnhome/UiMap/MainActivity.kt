package com.example.returnhome.UiMap

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.returnhome.network.RetrofitInstance
import com.example.returnhome.ViewModel.RouteViewModel
import com.example.returnhome.ViewModel.RouteViewModelFactory
import com.example.returnhome.ui.theme.ReturnHomeTheme
import com.example.returnhome.location.LocationClient
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    // Registrar el callback para permisos
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Manejar el resultado de la solicitud de permisos
        permissions.entries.forEach {
            println("${it.key} = ${it.value}")
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure osmdroid
        Configuration.getInstance().userAgentValue = packageName

        // Crea las dependencias con el contexto de la aplicación
        val locationClient = LocationClient(application)
        val api = RetrofitInstance.api

        setContent {
            ReturnHomeTheme {
                var showPermissionDialog by remember { mutableStateOf(false) }

                if (showPermissionDialog) {
                    AlertDialog(
                        onDismissRequest = { showPermissionDialog = false },
                        title = { Text("Permission Required") },
                        text = { Text("This app needs location permissions to work properly") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showPermissionDialog = false
                                    requestPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            ) {
                                Text("OK")
                            }
                        }
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: RouteViewModel = viewModel(
                        factory = RouteViewModelFactory(locationClient, api)
                    )

                    // Verifica permisos antes de mostrar el contenido principal
                    val permissionsState = rememberMultiplePermissionsState(
                        permissions = listOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )

                    if (permissionsState.allPermissionsGranted) {
                        MapScreen(viewModel)
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(
                                onClick = {
                                    if (permissionsState.shouldShowRationale) {
                                        showPermissionDialog = true
                                    } else {
                                        permissionsState.launchMultiplePermissionRequest()
                                    }
                                }
                            ) {
                                Text("Request Location Permissions")
                            }
                        }
                    }
                }
            }
        }
    }
}