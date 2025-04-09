package com.example.returnhome.utils

import android.content.Context
import android.location.Geocoder
import org.osmdroid.util.GeoPoint
import java.util.*

suspend fun geocodeAddress(context: Context, address: String): GeoPoint? {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocationName(address, 1)
        if (addresses?.isNotEmpty() == true) {
            GeoPoint(
                addresses[0].latitude,
                addresses[0].longitude
            )
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}