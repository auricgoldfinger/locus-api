package com.asamm.locus.api.sample

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import locus.api.android.utils.IntentHelper
import locus.api.objects.extra.Location
import locus.api.utils.Logger


class ActivityAverageLocation : FragmentActivity() {

    private var locationManager : LocationManager? = null
    private var latitudeList = mutableListOf<Double>()
    private var longitudeList = mutableListOf<Double>()
    private var altitudeList = mutableListOf<Double>()
    var txtAvgLat : TextView? = null
    var txtAvgLon : TextView? = null
    var txtAvgAlt : TextView? = null
    var txtAvgPoints : TextView? = null
    var txtLastLocation : TextView? = null

    //define the listener
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: android.location.Location) {

            val latitude = location.latitude
            latitudeList.add(latitude)
            val longitude = location.longitude
            longitudeList.add(longitude)
            val altitude = location.altitude
            altitudeList.add(altitude);

            txtLastLocation!!.text = ("${location.latitude} , ${location.longitude} (${location.altitude}m)")
            txtAvgLat!!.text = "${latitudeList.average()}"
            txtAvgLon!!.text = "${longitudeList.average()}"
            txtAvgAlt!!.text = "${altitudeList.average()}m"
            txtAvgPoints!!.text = "${latitudeList.size} points"
            Logger.logD(TAG, "\n\tAdded latitude: $latitude, longitude: $longitude, altitude $altitude" +
                    "\n\tAvg (${latitudeList.size}pt) ${latitudeList.average()}, ${longitudeList.average()} (${altitudeList.average()}m)")
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            Logger.logD(TAG, "$provider status change to $status")
        }
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    @SuppressLint("SetTextI18n")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_average_location)

        txtLastLocation = findViewById(R.id.txtLastPosition)
        txtAvgLat = findViewById(R.id.txtAvgLat)
        txtAvgLon = findViewById(R.id.txtAvgLon)
        txtAvgAlt = findViewById(R.id.txtAvgAlt)
        txtAvgPoints = findViewById(R.id.txtAvgPoints)

        val button: Button = findViewById(R.id.btnSetLocation)
        button.setOnClickListener {
            // stop listening for location
            stopLocationGathering()

            val avgLat = latitudeList.average()
            val avgLon = longitudeList.average()
            val avgAlt = altitudeList.average()

            Logger.logI(TAG, "Average after ${latitudeList.size} points: $avgLat, $avgLon - ${avgAlt}m")

            IntentHelper.sendGetLocationData(this@ActivityAverageLocation,
                    "$avgLon, $avgLat",
                    Location().apply {
                        latitude = avgLat
                        longitude = avgLon
                        altitude = avgAlt
                    })
            finish()
        }

        // finally check intent that started this sample
        startLocationGathering()
    }

    override fun onResume() {
        super.onResume()
        startLocationGathering()
    }

    fun startLocationGathering() {

        //GPS sensor
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?

        if (locationManager != null) {
            val criteria = Criteria()
            criteria.accuracy = Criteria.ACCURACY_FINE
            criteria.powerRequirement = Criteria.NO_REQUIREMENT
            criteria.isCostAllowed = false

            val bestProvider = locationManager!!.getBestProvider(criteria, true)

            if (bestProvider != null) {
                Logger.logD(TAG, "bestProvider: $bestProvider")

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {

                    // Request access
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this@ActivityAverageLocation, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        ActivityCompat.requestPermissions(this@ActivityAverageLocation, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                    } else {
                        ActivityCompat.requestPermissions(this@ActivityAverageLocation, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                    }
                }
                locationManager!!.requestLocationUpdates(bestProvider, 2000L, 0F, locationListener)
            }
        }
    }

    fun stopLocationGathering() {
        locationManager?.removeUpdates(locationListener)
        Logger.logI(TAG, "Location gathering stopped")
    }
    override fun onPause() {
        super.onPause()
        stopLocationGathering()
        Logger.logD(TAG, "onPause, done")
    }

    companion object {
        private const val TAG = "ActivityAverageLocation"
    }
}
