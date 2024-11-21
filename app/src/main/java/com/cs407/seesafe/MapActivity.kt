package com.cs407.seesafe

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.*
import kotlin.math.*

class MapActivity : AppCompatActivity() {

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private lateinit var mapView: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay
    private var pointsAdded = false
    private var username: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load osmdroid configuration
        Configuration.getInstance().load(applicationContext, androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext))

        setContentView(R.layout.activity_map)

        // Retrieve the username from Intent
        username = intent.getStringExtra("username")

        // Initialize MapView
        mapView = findViewById(R.id.map)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)

        // Request necessary permissions
        requestPermissionsIfNecessary(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
            // Add WRITE_EXTERNAL_STORAGE if you decide to handle storage for osmdroid
        ))
    }

    private fun requestPermissionsIfNecessary(permissions: Array<String>) {
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), REQUEST_PERMISSIONS_REQUEST_CODE)
        } else {
            initializeMap()
        }
    }

    private fun initializeMap() {
        // Initialize location overlay
        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(applicationContext), mapView)
        locationOverlay.enableMyLocation()
        locationOverlay.enableFollowLocation() // Centers the map on the user's location
        mapView.overlays.add(locationOverlay)

        // Set a listener for location changes
        locationOverlay.runOnFirstFix {
            runOnUiThread {
                if (!pointsAdded) {
                    addRandomMarkers(locationOverlay.myLocation)
                    pointsAdded = true
                }
            }
        }
    }

    private fun addRandomMarkers(center: GeoPoint?) {
        if (center == null) {
            Toast.makeText(this, "Unable to determine current location.", Toast.LENGTH_SHORT).show()
            return
        }

        val random = Random()
        val radiusMiles = 0.5

        for (i in 1..10) {
            val randomPoint = getRandomGeoPoint(center, radiusMiles)
            val marker = Marker(mapView)
            marker.position = randomPoint
            marker.icon = ContextCompat.getDrawable(this, R.drawable.ic_menu_mapmode)
            marker.title = "User $i"
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            mapView.overlays.add(marker)
        }

        mapView.invalidate()
    }

    /**
     * Generates a random GeoPoint within the specified radius (in miles) from the center point.
     */
    private fun getRandomGeoPoint(center: GeoPoint, radiusInMiles: Double): GeoPoint {
        val radiusInMeters = radiusInMiles * 1609.34 // Convert miles to meters
        val random = Random()
        val distance = random.nextDouble() * radiusInMeters
        val bearing = random.nextDouble() * 360

        val R = 6371000.0 // Earth radius in meters
        val radDist = distance / R
        val radBearing = Math.toRadians(bearing)

        val lat1 = Math.toRadians(center.latitude)
        val lon1 = Math.toRadians(center.longitude)

        val lat2 = asin(sin(lat1) * cos(radDist) + cos(lat1) * sin(radDist) * cos(radBearing))
        val lon2 = lon1 + atan2(
            sin(radBearing) * sin(radDist) * cos(lat1),
            cos(radDist) - sin(lat1) * sin(lat2)
        )

        val newLat = Math.toDegrees(lat2)
        val newLon = Math.toDegrees(lon2)

        return GeoPoint(newLat, newLon)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }

            if (allGranted) {
                initializeMap()
            } else {
                // Determine if any permission was permanently denied
                val permanentlyDenied = permissions.any {
                    ActivityCompat.shouldShowRequestPermissionRationale(this, it).not()
                }

                if (permanentlyDenied) {
                    // User selected "Don't ask again" for at least one permission
                    AlertDialog.Builder(this)
                        .setTitle("Permissions Required")
                        .setMessage("Location permissions are essential for displaying the map. Please enable them in the app settings.")
                        .setPositiveButton("Open Settings") { dialog, which ->
                            // Open app settings
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri = Uri.fromParts("package", packageName, null)
                            intent.data = uri
                            startActivity(intent)
                        }
                        .setNegativeButton("Cancel") { dialog, which ->
                            // Navigate back to VolunteerHomePage
                            navigateBackToHome()
                        }
                        .setCancelable(false)
                        .show()
                } else {
                    // Permissions denied without "Don't ask again"
                    Toast.makeText(this, "Permissions not granted. Unable to display map.", Toast.LENGTH_LONG).show()

                    // Navigate back to VolunteerHomePage
                    navigateBackToHome()
                }
            }
        }
    }

    private fun navigateBackToHome() {
        val intent = Intent(this, VolunteerHomePage::class.java)
        // Pass the username back if needed
        if (!username.isNullOrEmpty()) {
            intent.putExtra("username", username)
        }
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume() // Necessary for compass, my location overlays, etc.
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()  // Necessary for compass, my location overlays, etc.
    }
}

