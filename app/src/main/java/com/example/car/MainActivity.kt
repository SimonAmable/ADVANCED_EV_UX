package com.example.car

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.car.databinding.ActivityMainBinding
import androidx.fragment.app.Fragment
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLngBounds
import android.widget.TextView
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat


import android.widget.EditText
import android.widget.Button

import android.location.Geocoder
import java.util.Locale






import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Callback
import okhttp3.Call
import okhttp3.Response
import android.graphics.Color

import java.io.IOException

import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import org.json.JSONObject



class MainActivity : AppCompatActivity(), OnMapReadyCallback{

    private lateinit var binding : ActivityMainBinding
    private lateinit var mMap: GoogleMap
    private lateinit var directionTimeDisplay: TextView
    private lateinit var locationSearch: EditText
    private lateinit var destinationSearch: EditText
    private lateinit var searchButton: Button



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate RUN")


        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        replaceFragment(Map())


        directionTimeDisplay = findViewById(R.id.directionTimeDisplay)
        locationSearch = findViewById(R.id.location)
        destinationSearch = findViewById(R.id.destination)
        searchButton = findViewById(R.id.searchButton)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // For lower APIs
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }

        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.map -> {
                    locationSearch.visibility = View.VISIBLE
                    destinationSearch.visibility = View.VISIBLE
                    searchButton.visibility = View.VISIBLE
                    // Load map fragment
                    val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
                        ?: SupportMapFragment.newInstance().also {
                            supportFragmentManager.beginTransaction().replace(R.id.frame_layout, it).commit()
                        }
                    mapFragment.getMapAsync(this@MainActivity)
                    true
                }
                R.id.settings -> {
                    directionTimeDisplay.visibility = View.INVISIBLE
                    locationSearch.visibility = View.INVISIBLE
                    destinationSearch.visibility = View.INVISIBLE
                    searchButton.visibility = View.INVISIBLE
                    replaceFragment(Settings())
                }

                R.id.accessories -> {
                    directionTimeDisplay.visibility = View.INVISIBLE
                    locationSearch.visibility = View.INVISIBLE
                    destinationSearch.visibility = View.INVISIBLE
                    searchButton.visibility = View.INVISIBLE
                    replaceFragment(Accessories())
                }

                else -> {

                }
            }
            true
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        searchButton.setOnClickListener {

            // RESET POLYLINE
            locationSearcher()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style))
        val carletonUniversity = LatLng(45.3875812, -75.6960202)
        mMap.addMarker(
            MarkerOptions()
                .position(carletonUniversity)
                .title("Carleton University")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)) // Set marker color to blue
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(carletonUniversity, 6f))

        fetchAndDrawRoute(LatLng(45.3875812, -75.6960202), LatLng(45.423594, -75.700929), "AIzaSyA8ittymWIkgh_6jVb3aDCTUcK25DN6m7c")

    }

    private lateinit var locationCoords: LatLng
    private lateinit var destinationCoords: LatLng
    private var geocodeOperationsCompleted = 0 // Make it a member variable

    private fun locationSearcher() {
        val locationEditText = findViewById<EditText>(R.id.location)
        val destinationEditText = findViewById<EditText>(R.id.destination)

        val location = locationEditText.text.toString().trim()
        val destination = destinationEditText.text.toString().trim()

        if (location.isEmpty() || destination.isEmpty()) {
            return
        }

        geocodeOperationsCompleted = 0 // Reset the counter

        // Geocode the starting location
        geocodeLocation(location) { locationCoordinates ->
            if (locationCoordinates != null) {
                locationCoords = locationCoordinates
                geocodeOperationsCompleted++
                checkAndExecuteRouteDrawing()
            } else {
                Log.d("LocationSearcher", "Starting location not found")
            }
        }

        // Geocode the destination
        geocodeLocation(destination) { destinationCoordinates ->
            if (destinationCoordinates != null) {
                destinationCoords = destinationCoordinates
                geocodeOperationsCompleted++
                checkAndExecuteRouteDrawing()
            } else {
                Log.d("LocationSearcher", "Destination not found")
            }
        }
    }

    private fun checkAndExecuteRouteDrawing() {
        // Ensure that both geocoding operations have completed
        if (geocodeOperationsCompleted == 2) {
            // Both locations have been successfully geocoded
            runOnUiThread {
                fetchAndDrawRoute(locationCoords, destinationCoords, "AIzaSyA8ittymWIkgh_6jVb3aDCTUcK25DN6m7c")
            }
        }
    }

    private fun geocodeLocation(location: String, callback: (LatLng?) -> Unit) {
        val geocodingUrl = "https://maps.googleapis.com/maps/api/geocode/json?address=$location&key=AIzaSyA8ittymWIkgh_6jVb3aDCTUcK25DN6m7c"

        val client = OkHttpClient()
        val request = Request.Builder().url(geocodingUrl).build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                e.printStackTrace()
                callback(null)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.body?.string()?.let { jsonData ->
                    val jsonObject = JSONObject(jsonData)
                    val results = jsonObject.getJSONArray("results")

                    if (results.length() > 0) {
                        val locationObject = results.getJSONObject(0)
                        val geometry = locationObject.getJSONObject("geometry")
                        val location = geometry.getJSONObject("location")

                        val latitude = location.getDouble("lat")
                        val longitude = location.getDouble("lng")

                        callback(LatLng(latitude, longitude))
                    } else {
                        callback(null)
                    }
                }
            }
        })
    }

    private fun getDestinationLatLng(destination: String): LatLng {
        return LatLng(45.423594, -75.700929)
    }



    private fun replaceFragment(fragment : Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }

    private fun fetchAndDrawRoute(origin: LatLng, destination: LatLng, apiKey: String) {
        val client = OkHttpClient()
        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${origin.latitude},${origin.longitude}&" +
                "destination=${destination.latitude},${destination.longitude}&" +
                "key=$apiKey"

        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.body?.string()?.let { jsonData ->
                    val jsonObject = JSONObject(jsonData)
                    val routes = jsonObject.getJSONArray("routes")
                    if (routes.length() > 0) {
                        val route = routes.getJSONObject(0)
                        val legs = route.getJSONArray("legs")
                        val overviewPolyline = route.getJSONObject("overview_polyline")
                        val polyline = overviewPolyline.getString("points")

                        if (legs.length() > 0) {
                            val leg = legs.getJSONObject(0)
                            val durationObject = leg.getJSONObject("duration")
                            val durationText = durationObject.getString("text")
                            Log.d("Direction Time", durationText)

                            runOnUiThread {



                                val decodedPath = PolyUtil.decode(polyline)
                                // Add the line from origin to destination
                                mMap.addPolyline(
                                    PolylineOptions()
                                        .addAll(decodedPath)
                                        .color(Color.YELLOW)
                                        .width(15f)
                                )

                                // Calculate bounds that include origin and destination
                                val builder = LatLngBounds.builder()
                                builder.include(origin)
                                builder.include(destination)
                                decodedPath.forEach { point -> builder.include(point) }
                                val bounds = builder.build()

                                // Move camera to show the route
                                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 350))

                                // Display the direction time box
                                val drawable = GradientDrawable().apply {
                                    setColor(ContextCompat.getColor(this@MainActivity, R.color.background))
                                    cornerRadius = 20f
                                    setStroke(8, ContextCompat.getColor(this@MainActivity, R.color.white))
                                }

                                // Apply the drawable as the TextView background
                                directionTimeDisplay.background = drawable
                                directionTimeDisplay.text = "$durationText"
                                directionTimeDisplay.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }
        })
    }

}