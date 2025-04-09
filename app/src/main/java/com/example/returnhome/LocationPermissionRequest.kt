package com.example.returnhome

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionRequest(
    rationale: String,
    permissionsState: MultiplePermissionsState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Location Permission Required",
            modifier = Modifier.padding(16.dp),
            textAlign = TextAlign.Center
        )

        Text(
            text = rationale,
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (permissionsState.shouldShowRationale) {
                    // Show rationale dialog
                    android.app.AlertDialog.Builder(context)
                        .setTitle("Location Permission Needed")
                        .setMessage(rationale)
                        .setPositiveButton("OK") { _, _ ->
                            permissionsState.launchMultiplePermissionRequest()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                } else {
                    permissionsState.launchMultiplePermissionRequest()
                }
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Request Location Permission")
        }
    }
}