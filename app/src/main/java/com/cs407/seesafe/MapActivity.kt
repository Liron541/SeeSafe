package com.cs407.seesafe

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.*

class MapActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MapActivity" // Define TAG as a simple string
    }

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private lateinit var mapView: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay
    private var pointsAdded = false
    private var username: String? = null

    private lateinit var databaseReference: DatabaseReference
    private val blindUserMarkers = HashMap<String, Marker>()

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

        databaseReference = FirebaseDatabase.getInstance().getReference("blind_users_locations")

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
                listenToBlindUsersLocations()
            }
        }
    }

    private fun listenToBlindUsersLocations() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear existing blind user markers
                for (marker in blindUserMarkers.values) {
                    mapView.overlays.remove(marker)
                }
                blindUserMarkers.clear()

                for (child in snapshot.children) {
                    val blindUserLocation = child.getValue(BlindUserLocation::class.java)
                    if (blindUserLocation != null) {
                        val geoPoint = GeoPoint(blindUserLocation.latitude ?: 0.0, blindUserLocation.longitude ?: 0.0)
                        val marker = Marker(mapView)
                        marker.position = geoPoint
                        marker.icon = ContextCompat.getDrawable(this@MapActivity, R.drawable.ic_menu_mapmode) // Use a distinct icon
                        marker.title = "Blind User"
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        mapView.overlays.add(marker)

                        // Optionally, store markers by ID for future reference
                        blindUserMarkers[blindUserLocation.id ?: ""] = marker
                    }
                }
                mapView.invalidate()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MapActivity, "Failed to load blind users' locations.", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Database error: ${error.message}")
            }
        })
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

