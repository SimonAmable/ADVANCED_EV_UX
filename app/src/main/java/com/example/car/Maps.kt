package com.example.car

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import com.android.volley.toolbox.JsonObjectRequest
import com.example.car.databinding.FragmentMapsBinding
import com.android.volley.toolbox.Volley
import com.android.volley.Request as VolleyRequest
import com.android.volley.Response as VolleyResponse
import android.graphics.Bitmap
import android.graphics.Canvas
import com.google.android.gms.location.FusedLocationProviderClient
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import android.location.Location
import android.widget.Toast
import com.google.android.gms.location.LocationServices













class Maps : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    private lateinit var mMap: GoogleMap
    private lateinit var directionTimeDisplay: TextView
    private lateinit var locationSearch: EditText
    private lateinit var destinationSearch: EditText
    private lateinit var searchButton: ImageButton

    private lateinit var locationCoords: LatLng
    private lateinit var destinationCoords: LatLng
    private var geocodeOperationsCompleted = 0

    private lateinit var fusedLocationClient: FusedLocationProviderClient


    private var defaultRadius = 5
    private var defaultLimit = 30
    private var defaultLongitude = -75.6960
    private var defaultLatitude = 45.3876


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun getNearbyChargeStations(callback: (List<LatLng>) -> Unit) {

            val coordinatesList = mutableListOf<LatLng>()
            val url =
                "https://developer.nrel.gov/api/alt-fuel-stations/v1/nearest.json?api_key=Oyedfyv5EFGYpERGMRJhp8gIDTop6n5GMBrVj4lI&longitude=${defaultLongitude}&latitude=${defaultLatitude}&fuel_type=ELEC&limit=${defaultLimit}&radius=${defaultRadius}&country=CA"
            val queue = Volley.newRequestQueue(requireContext())
            val jsonObjectRequest = JsonObjectRequest(
                VolleyRequest.Method.GET, url, null,
                { response ->
                    Log.d("MainActivity", "Api call success")
                    val jsonOBJ = JSONObject(response.toString())
                    val jsonFuelArr = jsonOBJ.getJSONArray("fuel_stations")
                    for (i in 0 until jsonFuelArr.length()) {
                        val singleFuelStation = jsonFuelArr.getJSONObject(i)
                        val latitude = singleFuelStation.getDouble("latitude")
                        val longitude = singleFuelStation.getDouble("longitude")
                        coordinatesList.add(LatLng(latitude, longitude))
                    }
                    Log.d("ELEC CHARGING LIST", "$coordinatesList")
                    callback(coordinatesList)
                }, {
                    Log.d("MainActivity", "Api call failure")
                    callback(emptyList()) // Handle error case
                }
            )
            queue.add(jsonObjectRequest)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        directionTimeDisplay = binding.directionTimeDisplay
        locationSearch = binding.location
        destinationSearch = binding.destination
        searchButton = binding.searchButton
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())



        val inflater = LayoutInflater.from(context)
        val customMarkerView = inflater.inflate(R.layout.map_custom_marker, null)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        getNearbyChargeStations { cordList ->
            Log.d("singleCord", cordList.toString())
            for (coordinate in cordList) {
                Log.d("singleCord", coordinate.toString())
                mMap.addMarker(
                    MarkerOptions()
                        .position(coordinate)
                        .title("Charging Station")
                        .icon(BitmapDescriptorFactory.fromBitmap(createBitmapFromView(customMarkerView)))
                )
                Log.d("MainActivity", "Latitude: ${coordinate.latitude}, Longitude: ${coordinate.longitude}")
                // move camera to see ev stations in specified radius from locaiton
            }
        }

        binding.searchButton.setOnClickListener {
            // RESET POLYLINE
            mMap.clear()
            locationSearcher()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.style))

        // Should go to current location - work in progress
        val carletonUniversity = LatLng(45.3875812, -75.6960202)
        mMap.addMarker(
            MarkerOptions()
                .position(carletonUniversity)
                .title("Carleton University")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)) // Set marker color to blue
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(carletonUniversity, 14f))

    }

    private fun createBitmapFromView(view: View): Bitmap {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }



    private fun locationSearcher() {
        val locationEditText = binding.location
        val destinationEditText = binding.destination

        val location = locationEditText.text.toString().trim()
        val destination = destinationEditText.text.toString().trim()

        if(location.isEmpty() || destination.isEmpty()) {
            return
        }

        // Reset the counter
        geocodeOperationsCompleted = 0

        // Geocode the starting location
        geocodeLocation(location) { locationCoordinates ->
            if (locationCoordinates != null) {
                locationCoords = locationCoordinates
                updateGeoCounterAndDrawRoute()
            } else {
                Log.d("LocationSearcher", "Starting location not found")
            }
        }

        // Geocode the destination
        geocodeLocation(destination) { destinationCoordinates ->
            if (destinationCoordinates != null) {
                destinationCoords = destinationCoordinates
                updateGeoCounterAndDrawRoute()
            } else {
                Log.d("LocationSearcher", "Destination not found")
            }
        }
    }

    // Update the counter and if has got both location, draw map
    private fun updateGeoCounterAndDrawRoute() {
        geocodeOperationsCompleted++
        if (geocodeOperationsCompleted == 2) {
            activity?.runOnUiThread {
            mMap.addMarker(
                  MarkerOptions()
                  .position(destinationCoords)
                  .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            mMap.addMarker(
                MarkerOptions()
                .position(locationCoords)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )

            fetchAndDrawRoute(locationCoords, destinationCoords, "AIzaSyA8ittymWIkgh_6jVb3aDCTUcK25DN6m7c")
            }
        }
    }

    // Get geocode based off name/address types
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

    // Fetch the route and draw it and draw the markers
    private fun fetchAndDrawRoute(origin: LatLng, destination: LatLng, apiKey: String) {
        Log.d("Ranfetch", "HIT")
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

                            activity?.runOnUiThread {

                                // Add the line from origin to destination
                                val decodedPath = PolyUtil.decode(polyline)
                                mMap.addPolyline(
                                    PolylineOptions()
                                        .addAll(decodedPath)
                                        .color(Color.YELLOW)
                                        .width(15f)
                                )
                                Log.d("test1", "test")

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
                                    setColor(ContextCompat.getColor(requireContext(), R.color.background))
                                    cornerRadius = 20f
                                    setStroke(8, ContextCompat.getColor(requireContext(), R.color.white))
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