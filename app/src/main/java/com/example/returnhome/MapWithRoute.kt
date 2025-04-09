package com.example.returnhome

import android.graphics.Color
import android.view.MotionEvent
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Polyline

@Composable
fun MapWithRoute(
    currentLocation: GeoPoint?,
    routePoints: List<GeoPoint>?,
    homeLocation: GeoPoint?,
    onMapClick: (lat: Double, lon: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Configuración inicial de OSMdroid
    Configuration.getInstance().userAgentValue = context.packageName

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                minZoomLevel = 3.0
                maxZoomLevel = 19.0

                // Configuración inicial del zoom
                controller.setZoom(15.0) // Zoom inicial cercano
            }
        },
        update = { mapView ->
            // Limpiar overlays existentes
            mapView.overlays.clear()

            // Configurar ubicación actual
            currentLocation?.let { location ->
                Marker(mapView).apply {
                    position = location
                    title = "Tu ubicación"
                    snippet = "Posición actual"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    mapView.overlays.add(this)
                }
            }

            // Configurar ubicación de casa
            homeLocation?.let { home ->
                Marker(mapView).apply {
                    position = home
                    title = "Casa"
                    snippet = "Tu dirección"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    mapView.overlays.add(this)
                }
            }

            // Configurar ruta
            routePoints?.takeIf { it.size > 1 }?.let { points ->
                Polyline(mapView).apply {
                    setPoints(points)
                    color = Color.BLUE
                    width = 10f
                    mapView.overlays.add(this)
                }
            }

            // Configurar clics en el mapa
            mapView.overlays.add(object : Overlay() {
                override fun onSingleTapConfirmed(e: MotionEvent?, mapView: MapView?): Boolean {
                    e?.let {
                        val projection = mapView?.projection
                        projection?.let {
                            val point = it.fromPixels(e.x.toInt(), e.y.toInt())
                            onMapClick(point.latitude, point.longitude)
                        }
                    }
                    return true
                }
            })

            // Centrar y ajustar el zoom del mapa
            // Centrar y ajustar el zoom del mapa
            val pointsToShow = listOfNotNull(currentLocation, homeLocation)
            when {
                // Si tenemos ambos puntos (ubicación y casa)
                pointsToShow.size == 2 -> {
                    val boundingBox = BoundingBox(
                        pointsToShow.maxOf { it.latitude },
                        pointsToShow.maxOf { it.longitude },
                        pointsToShow.minOf { it.latitude },
                        pointsToShow.minOf { it.longitude }
                    )
                    mapView.post {
                        // Usamos la versión con 3 parámetros:
                        // boundingBox, animated, borderSizeInPixels
                        mapView.zoomToBoundingBox(boundingBox, false, 100)

                        // Opcional: Ajustar un poco más el zoom si es necesario
                        mapView.controller.zoomIn()
                    }
                }

                // Si solo tenemos un punto
                pointsToShow.isNotEmpty() -> {
                    mapView.post {
                        mapView.controller.setCenter(pointsToShow[0])
                        mapView.controller.setZoom(17.0) // Zoom más cercano para un solo punto
                    }
                }

                // Si no tenemos puntos, centrar en ubicación por defecto
                else -> {
                    mapView.post {
                        mapView.controller.setCenter(GeoPoint(19.4326, -99.1332)) // Ejemplo: CDMX
                        mapView.controller.setZoom(12.0) // Zoom medio
                    }
                }
            
            }

            mapView.invalidate() // Refrescar el mapa
        }
    )
}