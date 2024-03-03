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
<<<<<<< Updated upstream




=======
import android.widget.EditText
import android.widget.Button
>>>>>>> Stashed changes
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
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.android.volley.Request as VolleyRequest
import com.android.volley.Response as VolleyResponse
import org.json.JSONArray

class MainActivity : AppCompatActivity(), OnMapReadyCallback{

    private lateinit var binding : ActivityMainBinding
  
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
import android.widget.Button
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Callback
import okhttp3.Call
import okhttp3.Response
import android.graphics.Color
import android.widget.EditText
import java.io.IOException
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import org.json.JSONObject
import android.location.Geocoder
import java.util.Locale

class MainActivity : AppCompatActivity(), OnMapReadyCallback{

    private lateinit var binding : ActivityMainBinding
    private lateinit var mMap: GoogleMap
    private lateinit var directionTimeDisplay: TextView
    private lateinit var locationSearch: EditText
    private lateinit var destinationSearch: EditText
    private lateinit var searchButton: Button

    private lateinit var mMap: GoogleMap
    private lateinit var directionTimeDisplay: TextView

    private fun apiCall(callback: (List<LatLng>) -> Unit) {
        val coordinatesList = mutableListOf<LatLng>()
        val url =
            "https://developer.nrel.gov/api/alt-fuel-stations/v1/nearest.json?api_key=DEMO_KEY&location=K1S5B6&fuel_type=ELEC&limit=100&radius=400"
        val queue = Volley.newRequestQueue(this)
        val jsonObjectRequest = JsonObjectRequest(
            VolleyRequest.Method.GET, url, null,
            VolleyResponse.Listener { response ->
                Log.d("MainActivity", "Api call success")
                val jsonOBJ = JSONObject(response.toString())
                val jsonFuelArr = jsonOBJ.getJSONArray("fuel_stations")
                for (i in 0 until jsonFuelArr.length()) {
                    val singleFuelStation = jsonFuelArr.getJSONObject(i)
                    val latitude = singleFuelStation.getDouble("latitude")
                    val longitude = singleFuelStation.getDouble("longitude")
                    coordinatesList.add(LatLng(latitude, longitude))
                }
                callback(coordinatesList)
            }, VolleyResponse.ErrorListener {
                Log.d("MainActivity", "Api call failure")
                callback(emptyList()) // Handle error case
            }
        )
        queue.add(jsonObjectRequest)
    }
    override fun onCreate(savedInstanceState: Bundle?) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MainActivity", "onCreate RUN")

        Log.d("MainActivity", "onCreate RUN")



        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        replaceFragment(Map())

        directionTimeDisplay = findViewById(R.id.directionTimeDisplay)
        locationSearch = findViewById(R.id.location)
        destinationSearch = findViewById(R.id.destination)
        searchButton = findViewById(R.id.searchButton)


        directionTimeDisplay = findViewById(R.id.directionTimeDisplay)

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
                R.id.map -> replaceFragment(Map())
                R.id.settings -> replaceFragment(Settings())

                R.id.accessories -> replaceFragment(Accessories())
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
                R.id.map -> {
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
                    replaceFragment(Settings())
                }

                R.id.accessories -> {
                    directionTimeDisplay.visibility = View.INVISIBLE
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

    private fun locationSearcher() {
        
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

        // loads local carging stations from api
        // TODO : connect api to a lightning bolt button
        apiCall { cordList ->
            Log.d("singleCord", cordList.toString())
            for (coordinate in cordList) {
                Log.d("singleCord", coordinate.toString())
                mMap.addMarker(
                    MarkerOptions()
                        .position(coordinate)
                        .title("Charging Station")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                )
                Log.d("MainActivity", "Latitude: ${coordinate.latitude}, Longitude: ${coordinate.longitude}")
            }
        }

    }

<<<<<<< Updated upstream
=======
    private fun locationSearcher() {
        val locationEditText = findViewById<EditText>(R.id.location)
        val destinationEditText = findViewById<EditText>(R.id.destination)

        val location = locationEditText.text.toString().trim()
        val destination = destinationEditText.text.toString().trim()

        // Geocode user input to get coordinates
        geocodeLocation(location) { locationCoordinates ->
            if (locationCoordinates != null) {
                // Coordinates for the location found, call fetchAndDrawRoute
                fetchAndDrawRoute(locationCoordinates, getDestinationLatLng(destination), "AIzaSyA8ittymWIkgh_6jVb3aDCTUcK25DN6m7c")
            } else {
                // Handle error, location not found
                Log.d("LocationSearcher", "Location not found")
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
        // You can implement a similar function to geocode the destination if needed
        // For simplicity, you can return a predefined destination LatLng for now
        return LatLng(45.423594, -75.700929)
    }

>>>>>>> Stashed changes

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