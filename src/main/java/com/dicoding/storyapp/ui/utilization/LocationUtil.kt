package com.dicoding.storyapp.ui.utilization

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient

class LocationUtil(private val context: Context) {

    private var fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    fun getCurrentLocation(
        onLocationFetched: (LatLng) -> Unit,
        requestPermissionLauncher: ActivityResultLauncher<String>
    ) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val currentLocation = LatLng(it.latitude, it.longitude)
                    onLocationFetched(currentLocation)
                } ?: run {
                    Toast.makeText(context, "Failed to fetch location", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}
