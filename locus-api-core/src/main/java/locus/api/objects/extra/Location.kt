/*
 * Copyright 2012, Asamm Software, s. r. o.
 *
 * This file is part of LocusAPI.
 *
 * LocusAPI is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * LocusAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the Lesser GNU General Public
 * License along with LocusAPI. If not, see
 * <http://www.gnu.org/licenses/lgpl.html/>.
 */

package locus.api.objects.extra

import com.asamm.loggerV2.logE
import locus.api.objects.Storable
import locus.api.utils.*
import java.io.IOException

enum class LocationProvider(val textId: String?) {

    UNDEFINED(null),
    GPS("gps"),
    NETWORK("network")
}

/**
 * Container for the single location related information (simply pack of values at defined time moment).
 */
class Location() : Storable() {

    //*************************************************
    // CONTAINERS
    //*************************************************

    // SHORT

    private var extraDataShort: SparseArrayCompat<Short>? = null

    private fun getDataShort(key: Int): Short? {
        return extraDataShort?.get(key)
    }

    private fun setDataShort(key: Int, value: Short?) {
        if (value == null) {
            extraDataShort?.remove(key)
        } else {
            if (extraDataShort == null) {
                extraDataShort = SparseArrayCompat(0)
            }
            extraDataShort?.put(key, value)
        }
    }

    // INT

    private var extraDataInt: SparseArrayCompat<Int>? = null

    private fun getDataInt(key: Int): Int? {
        return extraDataInt?.get(key)
    }

    private fun setDataInt(key: Int, value: Int?) {
        if (value == null) {
            extraDataInt?.remove(key)
        } else {
            if (extraDataInt == null) {
                extraDataInt = SparseArrayCompat(0)
            }
            extraDataInt?.put(key, value)
        }
    }

    // LONG

    private var extraDataLong: SparseArrayCompat<Long>? = null

    private fun getDataLong(key: Int): Long? {
        return extraDataLong?.get(key)
    }

    private fun setDataLong(key: Int, value: Long?) {
        if (value == null) {
            extraDataLong?.remove(key)
        } else {
            if (extraDataLong == null) {
                extraDataLong = SparseArrayCompat(0)
            }
            extraDataLong?.put(key, value)
        }
    }

    // FLOAT

    private var extraDataFloat: SparseArrayCompat<Float>? = null

    private fun getDataFloat(key: Int): Float? {
        return extraDataFloat?.get(key)
    }

    private fun setDataFloat(key: Int, value: Float?) {
        if (value == null) {
            extraDataFloat?.remove(key)
        } else {
            if (extraDataFloat == null) {
                extraDataFloat = SparseArrayCompat(0)
            }
            extraDataFloat?.put(key, value)
        }
    }

    // DOUBLE

    private var extraDataDouble: SparseArrayCompat<Double>? = null

    private fun getDataDouble(key: Int): Double? {
        return extraDataDouble?.get(key)
    }

    private fun setDataDouble(key: Int, value: Double?) {
        if (value == null) {
            extraDataDouble?.remove(key)
        } else {
            if (extraDataDouble == null) {
                extraDataDouble = SparseArrayCompat(0)
            }
            extraDataDouble?.put(key, value)
        }
    }

    // STRING

    private var extraDataString: SparseArrayCompat<String>? = null

    private fun getDataString(key: Int): String? {
        return extraDataString?.get(key)
    }

    private fun setDataString(key: Int, value: String?) {
        if (value == null) {
            extraDataString?.remove(key)
        } else {
            if (extraDataString == null) {
                extraDataString = SparseArrayCompat(0)
            }
            extraDataString?.put(key, value)
        }
    }

    //*************************************************
    // CONSTRUCTION
    //*************************************************

    constructor(lat: Double, lon: Double) : this() {
        latitude = lat
        longitude = lon
    }

    constructor(loc: Location) : this() {
        set(loc)
    }

    /**
     * Sets the contents of the location to the values from the given location.
     */
    fun set(loc: Location) {
        id = loc.id
        provider = loc.provider
        time = loc.time
        latitude = loc.latitude
        longitude = loc.longitude

        // set extra data
        extraDataShort?.clear()
        loc.extraDataShort
            ?.takeIf { !it.isEmpty }
            ?.let {
                for (i in 0 until it.size()) {
                    setDataShort(it.keyAt(i), it.valueAt(i))
                }
            }
        extraDataInt?.clear()
        loc.extraDataInt
            ?.takeIf { !it.isEmpty }
            ?.let {
                for (i in 0 until it.size()) {
                    setDataInt(it.keyAt(i), it.valueAt(i))
                }
            }
        extraDataLong?.clear()
        loc.extraDataLong
            ?.takeIf { !it.isEmpty }
            ?.let {
                for (i in 0 until it.size()) {
                    setDataLong(it.keyAt(i), it.valueAt(i))
                }
            }

        extraDataFloat?.clear()
        loc.extraDataFloat
            ?.takeIf { !it.isEmpty }
            ?.let {
                for (i in 0 until it.size()) {
                    setDataFloat(it.keyAt(i), it.valueAt(i))
                }
            }
        extraDataDouble?.clear()
        loc.extraDataDouble
            ?.takeIf { !it.isEmpty }
            ?.let {
                for (i in 0 until it.size()) {
                    setDataDouble(it.keyAt(i), it.valueAt(i))
                }
            }
        extraDataString?.clear()
        loc.extraDataString
            ?.takeIf { !it.isEmpty }
            ?.let {
                for (i in 0 until it.size()) {
                    setDataString(it.keyAt(i), it.valueAt(i))
                }
            }
    }

    //*************************************************
    // VARIABLES
    //*************************************************

    /**
     * Location unique ID.
     */
    var id: Long = -1L

    /**
     * Provider for location source.
     */
    var provider: String? = null

    /**
     * UTC time of this location (in ms).
     */
    var time: Long = 0L

    // COORDINATES

    /**
     * Latitude of location in WGS coordinates.
     */
    var latitude: Double = 0.0
        set(value) {
            field = when {
                value < -90.0 -> {
                    logE(tag = TAG) { "setLatitude($value), invalid latitude" }
                    -90.0
                }
                value > 90.0 -> {
                    logE(tag = TAG) { "setLatitude($value), invalid latitude" }
                    90.0
                }
                else -> value
            }
        }

    /**
     * Longitude of location in WGS coordinates.
     */
    var longitude: Double = 0.0
        set(value) {
            // perform checks on range
            var newValue = value
            if (newValue < -180.0) {
                newValue += 360.0
            } else if (newValue > 180.0) {
                newValue -= 360.0
            }

            // set value
            field = newValue
        }

    //*************************************************
    // BASIC GNSS VALUES
    //*************************************************

    // ALTITUDE

    /**
     * Altitude value of the location (in m).
     */
    var altitude: Double?
        get() = getDataDouble(EXTRA_KEY_ALTITUDE)
        set(value) = setDataDouble(EXTRA_KEY_ALTITUDE, value)

    // SPEED

    /**
     * Speed of the device in meters/second.
     */
    var speed: Float?
        get() = getDataFloat(EXTRA_KEY_SPEED)
        set(value) = setDataFloat(EXTRA_KEY_SPEED, value)

    // BEARING

    /**
     * Direction of travel in degrees East of true North (in degree).
     */
    var bearing: Float?
        get() = getDataFloat(EXTRA_KEY_BEARING)
        set(value) {
            var bearingNew = value
            if (bearingNew != null) {
                while (bearingNew < 0.0f) {
                    bearingNew += 360.0f
                }
                while (bearingNew >= 360.0f) {
                    bearingNew -= 360.0f
                }
            }
            setDataFloat(EXTRA_KEY_BEARING, bearingNew)
        }

    // HORIZONTAL ACCURACY

    /**
     * Horizontal accuracy of the fix (in m).
     */
    var accuracyHor: Float?
        get() = getDataFloat(EXTRA_KEY_ACCURACY_HOR)
        set(value) = setDataFloat(EXTRA_KEY_ACCURACY_HOR, value)

    // VERTICAL ACCURACY

    /**
     * Vertical accuracy of the fix (in m).
     */
    var accuracyVer: Float?
        get() = getDataFloat(EXTRA_KEY_ACCURACY_VER)
        set(value) = setDataFloat(EXTRA_KEY_ACCURACY_VER, value)

    // ORIGINAL LATITUDE

    /**
     * Original, unmodified, longitude value.
     */
    var latitudeOriginal: Double?
        get() = getDataDouble(EXTRA_KEY_ORIG_LATITUDE)
        set(value) = setDataDouble(EXTRA_KEY_ORIG_LATITUDE, value)

    // ORIGINAL LONGITUDE

    /**
     * Original, unmodified, longitude value.
     */
    var longitudeOriginal: Double?
        get() = getDataDouble(EXTRA_KEY_ORIG_LONGITUDE)
        set(value) = setDataDouble(EXTRA_KEY_ORIG_LONGITUDE, value)

    // ORIGINAL ALTITUDE

    /**
     * Original, unmodified, altitude value.
     */
    var altitudeOriginal: Double?
        get() = getDataDouble(EXTRA_KEY_ORIG_ALTITUDE)
        set(value) = setDataDouble(EXTRA_KEY_ORIG_ALTITUDE, value)

    //*************************************************
    // SENSOR VALUES
    //*************************************************

    // CADENCE

    /**
     * Cadence value. If hasCadence() is false, 0 is returned.
     */
    var sensorCadence: Short?
        get() = getDataShort(EXTRA_KEY_SENSOR_CADENCE)
        set(value) = setDataShort(EXTRA_KEY_SENSOR_CADENCE, value)

    // HEART RATE

    /**
     * Heart rate value in BMP. If hasSensorHeartRate() is false, 0 is returned.
     */
    var sensorHeartRate: Short?
        get() = getDataShort(EXTRA_KEY_SENSOR_HEART_RATE)
        set(value) = setDataShort(EXTRA_KEY_SENSOR_HEART_RATE, value)

    // SPEED FROM SENSOR

    /**
     * Speed of the device over ground in meters/second. This speed is defined only when
     * 'speed sensor' is connected and supply valid values.
     */
    var sensorSpeed: Float?
        get() = getDataFloat(EXTRA_KEY_SENSOR_SPEED)
        set(value) = setDataFloat(EXTRA_KEY_SENSOR_SPEED, value)

    // POWER

    /**
     * Power value of the fix in W. If hasSensorPower() is false, 0.0 is returned.
     */
    var sensorPower: Float?
        get() = getDataFloat(EXTRA_KEY_SENSOR_POWER)
        set(value) = setDataFloat(EXTRA_KEY_SENSOR_POWER, value)

    // STRIDES

    /**
     * The num of strides.
     */
    var sensorStrides: Int?
        get() = getDataInt(EXTRA_KEY_SENSOR_STRIDES)
        set(value) = setDataInt(EXTRA_KEY_SENSOR_STRIDES, value)

    // TEMPERATURE

    /**
     * Temperature value. If hasSensorTemperature() is false, 0.0f is returned.
     */
    var sensorTemperature: Float?
        get() = getDataFloat(EXTRA_KEY_SENSOR_TEMPERATURE)
        set(value) = setDataFloat(EXTRA_KEY_SENSOR_TEMPERATURE, value)

    //*************************************************
    // GNSS META-DATA
    //*************************************************

    // GNSS QUALITY

    var gnssQuality: Short?
        get() = getDataShort(EXTRA_KEY_GNSS_QUALITY)
        set(value) = setDataShort(EXTRA_KEY_GNSS_QUALITY, value)

    // GNSS, HDOP

    /**
     * Horizontal dilution of precision for current location.
     *
     * More info: https://en.wikipedia.org/wiki/Dilution_of_precision_(navigation).
     */
    var gnssHdop: Float?
        get() = getDataFloat(EXTRA_KEY_GNSS_HDOP)
        set(value) = setDataFloat(EXTRA_KEY_GNSS_HDOP, value)

    // GNSS, VDOP

    /**
     * Vertical dilution of precision for current location.
     *
     * More info: https://en.wikipedia.org/wiki/Dilution_of_precision_(navigation).
     */
    var gnssVdop: Float?
        get() = getDataFloat(EXTRA_KEY_GNSS_VDOP)
        set(value) = setDataFloat(EXTRA_KEY_GNSS_VDOP, value)

    // GNSS, PDOP

    /**
     * Position (3D) dilution of precision for current location.
     *
     * More info: https://en.wikipedia.org/wiki/Dilution_of_precision_(navigation).
     */
    var gnssPdop: Float?
        get() = getDataFloat(EXTRA_KEY_GNSS_PDOP)
        set(value) = setDataFloat(EXTRA_KEY_GNSS_PDOP, value)

    // GNSS, NUMBER OF USED SATS

    /**
     * Number of used satellites used to obtain current location.
     */
    var gnssSatsUsed: Short?
        get() = getDataShort(EXTRA_KEY_GNSS_SATS_USED)
        set(value) = setDataShort(EXTRA_KEY_GNSS_SATS_USED, value)

    // GNSS, NUMBER OF VISIBLE SATS

    /**
     * Number of used satellites used to obtain current location.
     */
    var gnssSatsVisible: Short?
        get() = getDataShort(EXTRA_KEY_GNSS_SATS_VISIBLE)
        set(value) = setDataShort(EXTRA_KEY_GNSS_SATS_VISIBLE, value)

    /**
     * NTRIP mount point.
     */
    var gnssNtripMountPoint: String?
        get() = getDataString(EXTRA_KEY_GNSS_NTRIP_MOUNTPOINT)
        set(value) = setDataString(EXTRA_KEY_GNSS_NTRIP_MOUNTPOINT, value)

    /**
     * UTC time of observation start that created current location object (in ms).
     */
    var gnssObservationTimeStart: Long?
        get() = getDataLong(EXTRA_KEY_GNSS_OBSERVATION_TIME_START)
        set(value) = setDataLong(EXTRA_KEY_GNSS_OBSERVATION_TIME_START, value)

    /**
     * UTC time of observation end that created current location object (in ms).
     */
    var gnssObservationTimeEnd: Long?
        get() = getDataLong(EXTRA_KEY_GNSS_OBSERVATION_TIME_END)
        set(value) = setDataLong(EXTRA_KEY_GNSS_OBSERVATION_TIME_END, value)

    /**
     * Offset of the hardware antenna phase center (in m).
     * Variable [altitude] should already contain correct reduced value.
     * Variable [altitudeOriginal] should contain original measured value.
     */
    var extraAntennaPhaseCenterOffset: Float?
        get() = getDataFloat(EXTRA_KEY_EXTRA_ANTENNA_PHASE_CENTER_OFFSET)
        set(value) = setDataFloat(EXTRA_KEY_EXTRA_ANTENNA_PHASE_CENTER_OFFSET, value)

    /**
     * Height of the pole during measurement (in m).
     * Variable [altitude] should already contain correct reduced value.
     * Variable [altitudeOriginal] should contain original measured value.
     */
    var extraPoleHeight: Float?
        get() = getDataFloat(EXTRA_KEY_EXTRA_POLE_HEIGHT)
        set(value) = setDataFloat(EXTRA_KEY_EXTRA_POLE_HEIGHT, value)

    /**
     * GSM signal strength at certain moment (in %).
     */
    var extraGsmSignalStrength: Int?
        get() = getDataInt(EXTRA_KEY_EXTRA_GSM_SIGNAL_STRENGTH)
        set(value) = setDataInt(EXTRA_KEY_EXTRA_GSM_SIGNAL_STRENGTH, value)

    //*************************************************
    // BASIC EXTRA DATA
    //*************************************************

    // SPEED, OPTIMAL

    /**
     * Get stored speed useful for display to users or for compute with some operations, based on best
     * available speed. If speed from sensors is stored (more precise) it is returned. Otherwise
     * basic GPS speed is returned.
     *
     * @return speed for display purpose
     */
    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated(message = "Work with speed value directly")
    val speedOptimal: Float?
        get() = sensorSpeed ?: speed

    //*************************************************
    // TOOLS
    //*************************************************

    /**
     * Remove all attached sensors values.
     */
    fun removeSensorAll() {
        sensorCadence = null
        sensorHeartRate = null
        sensorPower = null
        sensorSpeed != null
        sensorStrides = null
        sensorTemperature != null
    }

    /**
     * Save current latitude/longitude/altitude values to the 'original' fields.
     */
    fun saveCurrentToOriginal() {
        latitudeOriginal = latitude
        longitudeOriginal = longitude
        altitudeOriginal = altitude
    }

    override fun toString(): String {
        return "Location [" +
                "tag: $provider, " +
                "time: $time, " +
                "lon: $longitude, " +
                "lat: $latitude, " +
                "alt: $altitude]"
    }

    // COMPUTATIONS

    /**
     * Returns the approximate distance in meters between this
     * location and the given location.  Distance is defined using
     * the WGS84 ellipsoid.
     *
     * @param dest the destination location
     * @return the approximate distance in meters
     */
    fun distanceTo(dest: Location): Float {
        val com = LocationCompute(this)
        return com.distanceTo(dest)
    }

    /**
     * Returns the approximate initial bearing in degrees East of true
     * North when traveling along the shortest path between this
     * location and the given location.  The shortest path is defined
     * using the WGS84 ellipsoid.  Locations that are (nearly)
     * antipodal may produce meaningless results.
     *
     * @param dest the destination location
     * @return the initial bearing in degrees
     */
    fun bearingTo(dest: Location): Float {
        val com = LocationCompute(this)
        return com.bearingTo(dest)
    }

    /**
     * Compute bearing and distance values at once
     *
     * @param dest the destination location
     * @return array with float[0] - distance (in metres),
     * float[1] - bearing (in degree)
     */
    fun distanceAndBearingTo(dest: Location): FloatArray {
        val com = LocationCompute(this)
        return floatArrayOf(com.distanceTo(dest), com.bearingTo(dest))
    }

    //*************************************************
    // STORABLE
    //*************************************************

    override fun getVersion(): Int {
        return 4
    }

    @Throws(IOException::class)
    override fun readObject(version: Int, dr: DataReaderBigEndian) {
        id = dr.readLong()
        provider = dr.readString().takeIf { it.isNotBlank() }
        time = dr.readLong()
        latitude = dr.readDouble()
        longitude = dr.readDouble()
        val hasAltitude = dr.readBoolean()
        val altitude = dr.readDouble()
        if (hasAltitude) {
            this.altitude = altitude
        }

        // red basic data
        if (dr.readBoolean()) {
            val hasAccuracy = dr.readBoolean()
            val accuracy = dr.readFloat()
            if (hasAccuracy) {
                this.accuracyHor = accuracy
            }
            val hasBearing = dr.readBoolean()
            val bearing = dr.readFloat()
            if (hasBearing) {
                this.bearing = bearing
            }
            val hasSpeed = dr.readBoolean()
            val speed = dr.readFloat()
            if (hasSpeed) {
                this.speed = speed
            }
        }

        // V1
        if (version >= 1) {
            // read sensor data
            if (dr.readBoolean()) {
                // read data from storage
                val extraSensor = if (version == 1) {
                    ExtraSensor().apply {
                        hasHr = dr.readBoolean()
                        hr = dr.readInt()
                        hasCadence = dr.readBoolean()
                        cadence = dr.readInt()
                        hasSpeed = dr.readBoolean()
                        speed = dr.readFloat()
                        hasPower = dr.readBoolean()
                        power = dr.readFloat()
                    }
                } else {
                    ExtraSensor().apply { read(dr) }
                }

                // map values to new system
                if (extraSensor.hasCadence) {
                    sensorCadence = extraSensor.cadence.toShort()
                }
                if (extraSensor.hasHr) {
                    sensorHeartRate = extraSensor.hr.toShort()
                }
                if (extraSensor.hasPower) {
                    sensorPower = extraSensor.power
                }
                if (extraSensor.hasSpeed) {
                    sensorSpeed = extraSensor.speed
                }
                if (extraSensor.hasStrides) {
                    sensorStrides = extraSensor.strides
                }
                if (extraSensor.hasTemperature) {
                    sensorTemperature = extraSensor.temperature
                }
            }
        }

        // V3
        var size: Byte
        if (version >= 3) {
            extraDataShort?.clear()
            size = dr.readByte()
            for (i in 0 until size) {
                setDataShort(dr.readByte().toInt(), dr.readShort())
            }
            extraDataInt?.clear()
            size = dr.readByte()
            for (i in 0 until size) {
                setDataInt(dr.readByte().toInt(), dr.readInt())
            }
            extraDataFloat?.clear()
            size = dr.readByte()
            for (i in 0 until size) {
                setDataFloat(dr.readByte().toInt(), dr.readFloat())
            }
            extraDataDouble?.clear()
            size = dr.readByte()
            for (i in 0 until size) {
                setDataDouble(dr.readByte().toInt(), dr.readDouble())
            }
        }

        // V4
        if (version >= 4) {
            extraDataLong?.clear()
            size = dr.readByte()
            for (i in 0 until size) {
                setDataLong(dr.readByte().toInt(), dr.readLong())
            }
            extraDataString?.clear()
            size = dr.readByte()
            for (i in 0 until size) {
                setDataString(dr.readByte().toInt(), dr.readString())
            }
        }
    }

    @Throws(IOException::class)
    override fun writeObject(dw: DataWriterBigEndian) {
        dw.writeLong(id)
        dw.writeString(provider)
        dw.writeLong(time)
        dw.writeDouble(latitude)
        dw.writeDouble(longitude)
        dw.writeBoolean(altitude != null)
        dw.writeDouble(altitude ?: 0.0)

        // write (deprecated) basic data
        if (accuracyHor != null || bearing != null || speed != null) {
            dw.writeBoolean(true)
            dw.writeBoolean(accuracyHor != null)
            dw.writeFloat(accuracyHor ?: 0.0f)
            dw.writeBoolean(bearing != null)
            dw.writeFloat(bearing ?: 0.0f)
            dw.writeBoolean(speed != null)
            dw.writeFloat(speed ?: 0.0f)
        } else {
            dw.writeBoolean(false)
        }

        // write sensors data (version 1+)
        val extraSensor = ExtraSensor().apply {
            if (sensorCadence != null) {
                hasCadence = true
                cadence = sensorCadence?.toInt() ?: 0
            }
            if (sensorHeartRate != null) {
                hasHr = true
                hr = sensorHeartRate?.toInt() ?: 0
            }
            if (sensorPower != null) {
                hasPower = true
                power = sensorPower ?: 0.0f
            }
            if (sensorSpeed != null) {
                hasSpeed = true
                speed = sensorSpeed ?: 0.0f
            }
            sensorStrides?.let {
                hasStrides = true
                strides = it
            }
            if (sensorTemperature != null) {
                hasTemperature = true
                temperature = sensorTemperature ?: 0.0f
            }
        }
        extraSensor
            .takeIf { it.hasData() }
            ?.let {
                dw.writeBoolean(true)
                it.write(dw)
            } ?: run {
            dw.writeBoolean(false)
        }

        // V3
        extraDataShort
            ?.takeIf { !it.isEmpty }
            ?.let {
                dw.writeByte(it.size().toByte())
                for (i in 0 until it.size()) {
                    dw.writeByte(it.keyAt(i).toByte())
                    dw.writeShort(it.valueAt(i).toInt())
                }
            }
            ?: run {
                dw.writeByte(0.toByte())
            }
        extraDataInt
            ?.takeIf { !it.isEmpty }
            ?.let {
                dw.writeByte(it.size().toByte())
                for (i in 0 until it.size()) {
                    dw.writeByte(it.keyAt(i).toByte())
                    dw.writeInt(it.valueAt(i))
                }
            }
            ?: run {
                dw.writeByte(0.toByte())
            }
        extraDataFloat
            ?.takeIf { !it.isEmpty }
            ?.let {
                dw.writeByte(it.size().toByte())
                for (i in 0 until it.size()) {
                    dw.writeByte(it.keyAt(i).toByte())
                    dw.writeFloat(it.valueAt(i))
                }
            }
            ?: run {
                dw.writeByte(0.toByte())
            }
        extraDataDouble
            ?.takeIf { !it.isEmpty }
            ?.let {
                dw.writeByte(it.size().toByte())
                for (i in 0 until it.size()) {
                    dw.writeByte(it.keyAt(i).toByte())
                    dw.writeDouble(it.valueAt(i))
                }
            }
            ?: run {
                dw.writeByte(0.toByte())
            }

        // V4
        extraDataLong
            ?.takeIf { !it.isEmpty }
            ?.let {
                dw.writeByte(it.size().toByte())
                for (i in 0 until it.size()) {
                    dw.writeByte(it.keyAt(i).toByte())
                    dw.writeLong(it.valueAt(i))
                }
            }
            ?: run {
                dw.writeByte(0.toByte())
            }
        extraDataString
            ?.takeIf { !it.isEmpty }
            ?.let {
                dw.writeByte(it.size().toByte())
                for (i in 0 until it.size()) {
                    dw.writeByte(it.keyAt(i).toByte())
                    dw.writeString(it.valueAt(i))
                }
            }
            ?: run {
                dw.writeByte(0.toByte())
            }

    }

    /**
     * Deprecated container for sensors data used only to keep compatibility with older
     * location object versions.
     */
    private class ExtraSensor : Storable() {

        var hasHr: Boolean = false
        var hr: Int = 0

        var hasCadence: Boolean = false
        var cadence: Int = 0

        var hasSpeed: Boolean = false
        var speed: Float = 0.0f

        var hasPower: Boolean = false
        var power: Float = 0.0f

        var hasStrides: Boolean = false
        var strides: Int = 0

        var hasTemperature: Boolean = false
        var temperature: Float = 0.0f

        fun hasData(): Boolean {
            return hasHr || hasCadence || hasSpeed
                    || hasPower || hasStrides || hasTemperature
        }

        override fun getVersion(): Int {
            return 1
        }

        @Throws(IOException::class)
        override fun readObject(version: Int, dr: DataReaderBigEndian) {
            hasHr = dr.readBoolean()
            hr = dr.readInt()
            hasCadence = dr.readBoolean()
            cadence = dr.readInt()
            hasSpeed = dr.readBoolean()
            speed = dr.readFloat()
            hasPower = dr.readBoolean()
            power = dr.readFloat()
            hasStrides = dr.readBoolean()
            strides = dr.readInt()
            dr.readBoolean() // hasBattery
            dr.readInt() // battery
            if (version >= 1) {
                hasTemperature = dr.readBoolean()
                temperature = dr.readFloat()
            }
        }

        @Throws(IOException::class)
        override fun writeObject(dw: DataWriterBigEndian) {
            dw.writeBoolean(hasHr)
            dw.writeInt(hr)
            dw.writeBoolean(hasCadence)
            dw.writeInt(cadence)
            dw.writeBoolean(hasSpeed)
            dw.writeFloat(speed)
            dw.writeBoolean(hasPower)
            dw.writeFloat(power)
            dw.writeBoolean(hasStrides)
            dw.writeInt(strides)
            dw.writeBoolean(false) // hasBattery
            dw.writeInt(0) // battery
            dw.writeBoolean(hasTemperature)
            dw.writeFloat(temperature)
        }
    }

    companion object {

        // tag for logger
        private const val TAG = "Location"

        private const val EXTRA_KEY_ALTITUDE = 10
        private const val EXTRA_KEY_SPEED = 11
        private const val EXTRA_KEY_BEARING = 12
        private const val EXTRA_KEY_ACCURACY_HOR = 13
        private const val EXTRA_KEY_ACCURACY_VER = 14

        private const val EXTRA_KEY_ORIG_LATITUDE = 15
        private const val EXTRA_KEY_ORIG_LONGITUDE = 16
        private const val EXTRA_KEY_ORIG_ALTITUDE = 17

        private const val EXTRA_KEY_SENSOR_HEART_RATE = 20
        private const val EXTRA_KEY_SENSOR_CADENCE = 21
        private const val EXTRA_KEY_SENSOR_SPEED = 22
        private const val EXTRA_KEY_SENSOR_TEMPERATURE = 23
        private const val EXTRA_KEY_SENSOR_POWER = 24
        private const val EXTRA_KEY_SENSOR_STRIDES = 25

        private const val EXTRA_KEY_GNSS_QUALITY = 51
        private const val EXTRA_KEY_GNSS_HDOP = 52
        private const val EXTRA_KEY_GNSS_VDOP = 53
        private const val EXTRA_KEY_GNSS_PDOP = 54
        private const val EXTRA_KEY_GNSS_SATS_USED = 55
        private const val EXTRA_KEY_GNSS_SATS_VISIBLE = 56
        private const val EXTRA_KEY_GNSS_NTRIP_MOUNTPOINT = 57
        private const val EXTRA_KEY_GNSS_OBSERVATION_TIME_START = 58
        private const val EXTRA_KEY_GNSS_OBSERVATION_TIME_END = 59

        private const val EXTRA_KEY_EXTRA_ANTENNA_PHASE_CENTER_OFFSET = 70
        private const val EXTRA_KEY_EXTRA_POLE_HEIGHT = 71
        private const val EXTRA_KEY_EXTRA_GSM_SIGNAL_STRENGTH = 72

        // internal method to visually verify IDs
        private fun validateIds(id: Int) {
            when (id) {
                EXTRA_KEY_ALTITUDE,
                EXTRA_KEY_SPEED,
                EXTRA_KEY_BEARING,
                EXTRA_KEY_ACCURACY_HOR,
                EXTRA_KEY_ACCURACY_VER,
                EXTRA_KEY_ORIG_LATITUDE,
                EXTRA_KEY_ORIG_LONGITUDE,
                EXTRA_KEY_ORIG_ALTITUDE,
                EXTRA_KEY_SENSOR_HEART_RATE,
                EXTRA_KEY_SENSOR_CADENCE,
                EXTRA_KEY_SENSOR_SPEED,
                EXTRA_KEY_SENSOR_TEMPERATURE,
                EXTRA_KEY_SENSOR_POWER,
                EXTRA_KEY_SENSOR_STRIDES,
                EXTRA_KEY_GNSS_QUALITY,
                EXTRA_KEY_GNSS_HDOP,
                EXTRA_KEY_GNSS_VDOP,
                EXTRA_KEY_GNSS_PDOP,
                EXTRA_KEY_GNSS_SATS_USED,
                EXTRA_KEY_GNSS_SATS_VISIBLE,
                EXTRA_KEY_GNSS_NTRIP_MOUNTPOINT,
                EXTRA_KEY_GNSS_OBSERVATION_TIME_START,
                EXTRA_KEY_GNSS_OBSERVATION_TIME_END,
                EXTRA_KEY_EXTRA_ANTENNA_PHASE_CENTER_OFFSET,
                EXTRA_KEY_EXTRA_POLE_HEIGHT,
                EXTRA_KEY_EXTRA_GSM_SIGNAL_STRENGTH -> {}
            }
        }
    }
}
