package com.example.firebaseandfoursquareapidemo.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.widget.Toast
import com.example.firebaseandfoursquareapidemo.Explore.Explore
import com.example.firebaseandfoursquareapidemo.Explore.Item_
import com.example.firebaseandfoursquareapidemo.Explore.Venue
import com.example.firebaseandfoursquareapidemo.R
import com.example.firebaseandfoursquareapidemo.network.FourSquareService
import com.example.firebaseandfoursquareapidemo.utils.Const
import com.example.firebaseandfoursquareapidemo.utils.Const.LOCATION_PERMISSION_REQUEST_CODE
import com.example.firebaseandfoursquareapidemo.utils.Utils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapMain : AppCompatActivity(), OnMapReadyCallback {

    private var query = "restaurant"

    var lstGroups = ArrayList<Item_>()
    private var googleMap: GoogleMap? = null
    private var locationManager: LocationManager? = null

    private lateinit var lat: String
    private lateinit var lng: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_main)

        val mapFragment: SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onResume() {
        super.onResume()
        if (!Utils.checkPermission(
                Manifest.permission.ACCESS_FINE_LOCATION,
                this
            ) && !Utils.checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, this)
        ) {
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        } else {
            if (Utils.isLocationEnabled(this)) {
                afterPermission()
            } else {
                Utils.displayPromptForEnablingGPS(this)
            }
        }
    }

    fun getNearByLocations() {

        if (Utils.isConnectedToInternet(this)) {
            val fourSquareService = FourSquareService.retrofit.create(FourSquareService::class.java)
            Log.d(">> ", "$lat,$lng")
            val call = fourSquareService.requestExplore(
                Const.Client_ID, Const.Client_Secret, Const.apiVersion, "$lat,$lng", query,
                "200"
            )
            call.enqueue(object : Callback<Explore> {
                override fun onResponse(call: Call<Explore>, response: Response<Explore>) {
                    lstGroups.clear()
                    lstGroups.addAll(response.body()!!.response.groups[0].items)
                    Log.d("lstGroups", "" + lstGroups.size)


                    googleMap!!.clear()

                    for (item in lstGroups) {
                        loadPins(item.venue)
                    }
                }

                override fun onFailure(call: Call<Explore>, t: Throwable) {
                    Log.d("onFailure", "" + t.localizedMessage)
                }
            })
        } else
            Toast.makeText(this, getString(R.string.check_internet), Toast.LENGTH_LONG).show()
    }

    private fun requestPermission(permission: String, permission2: String) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            Toast.makeText(this, getString(R.string.gps_msg), Toast.LENGTH_LONG).show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permission, permission2),
                Const.LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun moveCam() {
        try {
            val ltg = java.lang.Double.parseDouble(lat)
            val lng = java.lang.Double.parseDouble(lng)
            googleMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(ltg, lng), 18f))
            try {
                googleMap?.isMyLocationEnabled = true
            } catch (e: Exception) {
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(p0: GoogleMap?) {
        googleMap = p0
        try {
            googleMap?.isMyLocationEnabled = true
        } catch (e: Exception) {
        }
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location?) {
            try {
                lat = location!!.latitude.toString()
                lng = location.longitude.toString()
                moveCam()
                getNearByLocations()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {

        }

        override fun onProviderEnabled(provider: String) {

        }

        override fun onProviderDisabled(provider: String) {

        }
    }

    private fun afterPermission() {
        locationManager = baseContext.getSystemService(Activity.LOCATION_SERVICE) as LocationManager
        val LOCATION_PROVIDER = LocationManager.GPS_PROVIDER

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            return
        }
        val currentLoc: Location? = locationManager!!.getLastKnownLocation(LOCATION_PROVIDER)
        if (currentLoc == null) {

        } else {
            lat = currentLoc.latitude.toString()
            lng = currentLoc.longitude.toString()
            moveCam()
            getNearByLocations()
        }
        locationManager!!.requestLocationUpdates(
            locationManager!!.getBestProvider(Criteria(), true),
            0,
            5f,
            locationListener
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> try {
                for (permission in permissions) {
                    if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION, ignoreCase = true)) {
                        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            afterPermission()
                        } else {
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(
                                    permission,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                ),
                                Const.LOCATION_PERMISSION_REQUEST_CODE
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadPins(venue: Venue) {
        try {
            val ltd = java.lang.Double.parseDouble(venue.location.lat.toString())
            val lng = java.lang.Double.parseDouble(venue.location.lng.toString())

            val latLng = LatLng(ltd, lng)
            val markerOptions = MarkerOptions()
            markerOptions.position(latLng)
            markerOptions.title(venue.name)
            googleMap?.addMarker(markerOptions)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}