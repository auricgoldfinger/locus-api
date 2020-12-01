package com.asamm.locus.api.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import locus.api.android.ActionBasics
import locus.api.android.features.periodicUpdates.UpdateContainer
import locus.api.android.objects.LocusVersion
import locus.api.android.utils.IntentHelper
import locus.api.android.utils.LocusUtils
import locus.api.objects.extra.Location
import locus.api.utils.Logger
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class ActivityAverageLocusLocation : FragmentActivity() {

    // refresh handler
    private val handler: Handler = Handler(Looper.getMainLooper())

    // refresh interval (in ms)
    private val refreshInterval = TimeUnit.SECONDS.toMillis(1)

    // refresh task itself
    private val refresh: (() -> Unit) = {
        refreshContent()
    }

    private var latitudeList = mutableListOf<Double>()
    private var longitudeList = mutableListOf<Double>()
    private var altitudeList = mutableListOf<Double>()
    private lateinit var txtAvgLat: TextView
    private lateinit var txtAvgLon: TextView
    private lateinit var txtAvgAlt: TextView
    private lateinit var txtAvgPoints: TextView

    private lateinit var txtTime: TextView
    private lateinit var txtLastLocation: TextView
    private lateinit var txtLastAltitude: TextView
    private lateinit var txtSatsUsed: TextView
    private lateinit var txtAccuracy: TextView
    private lateinit var txtBearing: TextView
    private lateinit var txtSpeed: TextView
    private lateinit var txtTvInfo: TextView


    @SuppressLint("SetTextI18n")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_average_location)

        txtTime = findViewById(R.id.txtTime)
        txtLastLocation = findViewById(R.id.txtLastPosition)
        txtLastAltitude = findViewById(R.id.txtLastAltitude)
        txtSatsUsed = findViewById(R.id.txtSatsUsed)
        txtAccuracy = findViewById(R.id.txtAccuracy)
        txtBearing = findViewById(R.id.txtBearing)
        txtSpeed = findViewById(R.id.txtSpeed)

        txtAvgLat = findViewById(R.id.txtAvgLat)
        txtAvgLon = findViewById(R.id.txtAvgLon)
        txtAvgAlt = findViewById(R.id.txtAvgAlt)
        txtAvgPoints = findViewById(R.id.txtAvgPoints)

        txtTvInfo = findViewById(R.id.txtTvInfo)

        val button: FloatingActionButton = findViewById(R.id.btnSetLocation)
        button.setOnClickListener {
            // stop listening for location
            handler.removeCallbacks(refresh)

            val avgLat = latitudeList.average()
            val avgLon = longitudeList.average()
            val avgAlt = altitudeList.average()

            Logger.logI(TAG, "Average after ${latitudeList.size} points: $avgLat, $avgLon - ${avgAlt}m")

            IntentHelper.sendGetLocationData(this@ActivityAverageLocusLocation,
                    "$avgLon, $avgLat",
                    Location().apply {
                        latitude = avgLat
                        longitude = avgLon
                        hasAltitude = true
                        altitude = avgAlt
                    })
            finish()
        }

    }

    public override fun onStart() {
        super.onStart()

        // start refresh
        handler.post(refresh)
    }

    public override fun onStop() {
        super.onStop()

        // stop refresh flow
        handler.removeCallbacks(refresh)
    }

    /**
     * Perform refresh and request another update after defined interval.
     */
    private fun refreshContent() {
        try {
            Thread {
                LocusUtils.getActiveVersion(this)?.let { lv ->
                    ActionBasics.getUpdateContainer(this, lv)?.let { uc ->
                        handleUpdate(lv, uc)
                    } ?: {
                        handleUpdate(lv, null)
                        Logger.logW(TAG, "refreshContent(), unable to obtain `UpdateContainer`")
                    }()
                } ?: {
                    handleUpdate(null, null)
                    Logger.logW(TAG, "refreshContent(), unable to obtain `ActiveVersion`")
                }()

            }.start()
        } finally {
            handler.postDelayed(refresh, refreshInterval)
        }
    }

    /**
     * Handle fresh data.
     *
     * @param lv current Locus version
     * @param uc received container
     */
    @SuppressLint("SetTextI18n")
    private fun handleUpdate(lv: LocusVersion?, uc: UpdateContainer?) {
        handler.post {
            if (lv != null && uc != null) {

                val latitude = uc.locMyLocation.latitude
                latitudeList.add(latitude)
                val longitude = uc.locMyLocation.longitude
                longitudeList.add(longitude)
                val altitude = uc.locMyLocation.altitude
                altitudeList.add(altitude);

                txtTime.text = "GPS Time: ${SimpleDateFormat.getTimeInstance().format(Date(uc.locMyLocation.time))}"
                txtLastLocation.text = "Position: ${uc.locMyLocation.latitude} , ${uc.locMyLocation.longitude}"
                txtLastAltitude.text = "Altitude: ${uc.locMyLocation.altitude}m"
                txtSatsUsed.text = "Satellites: ${uc.gpsSatsUsed.toString()}/${uc.gpsSatsAll.toString()}"
                txtAccuracy.text = "Accuracy: ${uc.locMyLocation.accuracy.toString()}"
                txtBearing.text = "Bearing: ${uc.locMyLocation.bearing.toString()}"
                txtSpeed.text = "Speed: ${uc.locMyLocation.speed.toString()}"

                txtAvgLat.text = "${latitudeList.average()}"
                txtAvgLon.text = "${longitudeList.average()}"
                txtAvgAlt.text = "${altitudeList.average()}m"
                txtAvgPoints.text = "${latitudeList.size} points"

                txtTvInfo.text = "${SimpleDateFormat.getTimeInstance().format(Date())}, Locus v${lv.versionName}, battery: ${uc.deviceBatteryValue}%"

                Logger.logD(TAG, "\n\tAdded latitude: $latitude, longitude: $longitude, altitude $altitude" +
                        "\n\tAvg (${latitudeList.size}pt) ${latitudeList.average()}, ${longitudeList.average()} (${altitudeList.average()}m)")
            }
        }

    }

    companion object {
        private const val TAG = "ActivityAverageLocusLocation"
    }
}
