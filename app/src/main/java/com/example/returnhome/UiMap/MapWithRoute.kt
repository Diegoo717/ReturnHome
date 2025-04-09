package com.example.returnhome.UiMap

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

    Configuration.getInstance().userAgentValue = context.packageName

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                minZoomLevel = 3.0
                maxZoomLevel = 19.0

                controller.setZoom(15.0)
            }
        },
        update = { mapView ->
            mapView.overlays.clear()

            currentLocation?.let { location ->
                Marker(mapView).apply {
                    position = location
                    title = "Tu ubicación"
                    snippet = "Posición actual"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    mapView.overlays.add(this)
                }
            }

            homeLocation?.let { home ->
                Marker(mapView).apply {
                    position = home
                    title = "Casa"
                    snippet = "Tu dirección"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    mapView.overlays.add(this)
                }
            }

            routePoints?.takeIf { it.size > 1 }?.let { points ->
                Polyline(mapView).apply {
                    setPoints(points)
                    color = Color.BLUE
                    width = 10f
                    mapView.overlays.add(this)
                }
            }

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

            val pointsToShow = listOfNotNull(currentLocation, homeLocation)
            when {
                pointsToShow.size == 2 -> {
                    val boundingBox = BoundingBox(
                        pointsToShow.maxOf { it.latitude },
                        pointsToShow.maxOf { it.longitude },
                        pointsToShow.minOf { it.latitude },
                        pointsToShow.minOf { it.longitude }
                    )
                    mapView.post {
                        mapView.zoomToBoundingBox(boundingBox, false, 100)

                        mapView.controller.zoomIn()
                    }
                }

                pointsToShow.isNotEmpty() -> {
                    mapView.post {
                        mapView.controller.setCenter(pointsToShow[0])
                        mapView.controller.setZoom(17.0)
                    }
                }

                else -> {
                    mapView.post {
                        mapView.controller.setCenter(GeoPoint(19.4326, -99.1332))
                        mapView.controller.setZoom(12.0)
                    }
                }
            
            }

            mapView.invalidate()
        }
    )
}