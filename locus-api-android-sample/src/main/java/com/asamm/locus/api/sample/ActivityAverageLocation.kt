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
import kotlin.random.Random


class ActivityAverageLocation : FragmentActivity() {

    private var locationManager : LocationManager? = null
    private var latitudeList = mutableListOf<Double>()
    private var longitudeList = mutableListOf<Double>()
    private var altitudeList = mutableListOf<Double>()
    var txtLocationStatus : TextView? = null

    //define the listener
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: android.location.Location?) {
            if (location != null) {

                val latitude = location.latitude
                latitudeList.add(latitude)
                val longitude = location.longitude
                longitudeList.add(longitude)
                val altitude = location.altitude
                altitudeList.add(altitude);

                txtLocationStatus!!.text = ("(${latitudeList.size} pt) ${location.latitude} , ${location.longitude}")
                Logger.logD(TAG, "Added latitude: $latitude, longitude: $longitude, altitude $altitude")

                Logger.logD(TAG, "Now available: ${latitudeList.size} positions")

//            if(locationCounter == COUNTERRESET){
//
//                for(int i = 0; i < latitudeList.size();i++){
//                    latitudeAverage = latitudeAverage + latitudeList.get(i);
//                    longitudeAverage = longitudeAverage + longitudeList.get(i);
//                    altitudeAverage = altitudeAverage + altitudeList.get(i);
//                }
//
//                latitudeAverage = latitudeAverage / latitudeList.size();
//                longitudeAverage = longitudeAverage / longitudeList.size();
//                altitudeAverage = altitudeAverage / altitudeList.size();
//
//                locationUpdate = new Location(bestProvider);
//                location.setLatitude(latitudeAverage);
//                location.setLongitude(longitudeAverage);
//                location.setAltitude(altitudeAverage);
//
//                **HERE I WANT TO SET LOCATION UPDATE AS MY GPS POSITION**
//
//                locationCounter = 0;
//                latitudeAverage = 0;
//                longitudeAverage = 0;
//                altitudeAverage = 0;
//                latitudeList.clear();
//                longitudeList.clear();
//                altitudeList.clear();
//            }
            }
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

        txtLocationStatus = findViewById(R.id.txtAvgLocation)
        val button: Button = findViewById(R.id.btnSetLocation)
        button.setOnClickListener {
            // stop listening for location
            stopLocationGathering()

            IntentHelper.sendGetLocationData(this@ActivityAverageLocation,
                    "Non-sense Loc ;)",
                    Location().apply {
                        latitude = latitudeList.average()
                        longitude = longitudeList.average()
                        hasAltitude = true
                        altitude = altitudeList.average()
                    })
            finish()
        }
//        button.setOnClickListener(object : View.OnClickListener() {
//            override fun onClick(v: View?) {
//                // Code here executes on main thread after user presses button
//            }
//        })

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

            Logger.logD(TAG, "bestProvider: $bestProvider")

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            locationManager!!.requestLocationUpdates(bestProvider, 2000L, 0F, locationListener)
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
