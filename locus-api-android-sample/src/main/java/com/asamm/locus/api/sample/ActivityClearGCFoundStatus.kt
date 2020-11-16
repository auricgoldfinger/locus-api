package com.asamm.locus.api.sample

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import locus.api.android.ActionBasics
import locus.api.android.features.geocaching.fieldNotes.FieldNotesHelper
import locus.api.android.objects.LocusVersion
import locus.api.android.utils.IntentHelper
import locus.api.android.utils.LocusUtils
import locus.api.objects.geoData.Point
import locus.api.utils.Logger

class ActivityClearGCFoundStatus : FragmentActivity() {

    private val NO_GEOCACHE = 0
    private val STATUS_CLEARED = 1
    private val STATUS_ALREADY_CLEARED = -5
    private val ERROR_WHILE_CLEARING_STATUS = -1
    private val FIELDNOTE_AVAILABLE_STATUS_FIXED = -2
    private val FIELDNOTE_AVAILABLE_STATUS_NOT_FIXED = -3
    private val FIELDNOTE_AVAILABLE_STATUS_NOT_CLEARED = -4

    @SuppressLint("SetTextI18n")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tv = TextView(this)
        tv.text = "Clear Geocache Found Status - only for receiving Tools actions from cache"
        setContentView(tv)

        // finally check intent that started this sample
        checkStartIntent()
    }

    private fun checkStartIntent() {
        val intent = intent
        Logger.logD(TAG, "received intent: $intent")
        if (intent == null) {
            return
        }

        // get Locus from intent
//        intent.putExtra(LocusConst.INTENT_EXTRA_PACKAGE_NAME, "menion.android.locus.pro")
        val lv = LocusUtils.createLocusVersion(this, intent)
        if (lv == null) {
            Logger.logD(TAG, "checkStartIntent(), cannot obtain LocusVersion")
            return
        }

        if (IntentHelper.isIntentPointTools(intent)) {
            try {
                val pt = IntentHelper.getPointFromIntent(this, intent)
                if (pt == null) {
                    Toast.makeText(this@ActivityClearGCFoundStatus, "Wrong INTENT - no point!", Toast.LENGTH_SHORT).show()
                } else if (pt.gcData != null) {
                    handleResult(clearFoundStatus(pt, lv), pt)
                }
            } catch (e: Exception) {
                Logger.logE(TAG, "handle point tools", e)
            }
        } else if (IntentHelper.isIntentPointsTools(intent)) {
            val pointIds = IntentHelper.getPointsFromIntent(intent)
            if (pointIds == null || pointIds.isEmpty()) {
                AlertDialog.Builder(this@ActivityClearGCFoundStatus)
                        .setTitle("Intent - Points screen (Tools)")
                        .setMessage("Problem with loading waypointIds").setPositiveButton("Close") { _, _ -> }
                        .show()
            } else {
                clearFoundStatusFromCachesById(lv, pointIds)
            }
        } else {
            Toast.makeText(this@ActivityClearGCFoundStatus, "Cannot handle this!", Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    private fun clearFoundStatus(pt: Point, lv: LocusVersion):Int {
        var result = NO_GEOCACHE

        if (pt.gcData != null) {
            // This is a cache. Are there field notes?
            val fieldnotes = FieldNotesHelper.get(this@ActivityClearGCFoundStatus, lv, pt.gcData!!.cacheID)
            if (fieldnotes.size == 0 && pt.gcData!!.isFound) {
                // No fieldnotes but cache is marked as found. Clear it.

                //        pt.location.latitude = pt.location.latitude + 0.001
                //        pt.location.longitude = pt.location.longitude + 0.001
                //        pt.gcData!!.encodedHints = "Modified from point"
                //        pt.gcData!!.owner = "Auric G"
                //        pt.gcData!!.isFound = !pt.gcData!!.isFound;
                //        pt.gcData!!.isAvailable = !pt.gcData!!.isAvailable
                //        pt.gcData!!.type = (Math.random() * 13).toInt()
                //        Logger.logD(TAG, "Type is now " + pt.gcData!!.type)

                pt.gcData!!.isFound = false
                if (ActionBasics.updatePoint(this@ActivityClearGCFoundStatus, lv, pt, true) == 1) {
                    result = STATUS_CLEARED;
                } else {
                    result = ERROR_WHILE_CLEARING_STATUS
                }
            } else if (fieldnotes.size > 0 && !pt.gcData!!.isFound) {
                // Fieldnotes found, but cache isn't set to found. Fixing...
                pt.gcData!!.isFound = true
                if (ActionBasics.updatePoint(this@ActivityClearGCFoundStatus, lv, pt, true) == 1) {
                    result = FIELDNOTE_AVAILABLE_STATUS_FIXED
                } else {
                    result = FIELDNOTE_AVAILABLE_STATUS_NOT_FIXED
                }
            } else {
                if (fieldnotes.size > 0) result = FIELDNOTE_AVAILABLE_STATUS_NOT_CLEARED
                else result = STATUS_ALREADY_CLEARED

//                Toast.makeText(this@ActivityClearGCFoundStatus, "Toggle found status of ${pt.gcData!!.cacheID} ${pt.name}", Toast.LENGTH_SHORT).show()
//                pt.gcData!!.isFound = !pt.gcData!!.isFound
//                ActionBasics.updatePoint(this@ActivityClearGCFoundStatus, lv, pt, true)
//                Logger.logI(TAG, "Ahh, I secretly toggled the status, so now FOUND = ${pt.gcData!!.isFound}, which means that the following message is a lie.")
            }
        } else {
            Logger.logW(TAG, "Point ${pt.id} (${pt.name}) isn't a geocache")
            result = NO_GEOCACHE
        }

        return result
    }


    private fun clearFoundStatusFromCachesById(lv: LocusVersion, ptsIds: LongArray?) {
        if (ptsIds == null || ptsIds.isEmpty()) {
            Toast.makeText(this@ActivityClearGCFoundStatus, "No points to clear", Toast.LENGTH_SHORT).show()
            return
        }

        var clearCount = 0;
        var lastCache = ""

        for (wptId in ptsIds) {
            try {
                val pt = ActionBasics.getPoint(this@ActivityClearGCFoundStatus, lv, wptId)
                if (pt?.gcData != null) {
                    Logger.logD(TAG, "loadGeocachePointsFromLocus(), searched wptId:" + wptId + ", vs db point:" + pt.id)
                    val result = clearFoundStatus(pt, lv)
                    if (result == STATUS_CLEARED) {
                        ++clearCount
                        lastCache = pt.gcData!!.cacheID + ": " + pt.name
                    }

                    handleResult(result, pt)
                }
            } catch (e: Exception) {
                Logger.logE(TAG, "loadPointsFromLocus($ptsIds)", e)
            }
        }

        if (clearCount == 1) {
            Toast.makeText(this@ActivityClearGCFoundStatus, "Found status cleared for '$lastCache'", Toast.LENGTH_SHORT).show()
        } else if (clearCount > 1)
            Toast.makeText(this@ActivityClearGCFoundStatus, "Found status cleared for for '$lastCache' and  ${clearCount - 1} others", Toast.LENGTH_SHORT).show()
        else
            Logger.logW(TAG, "No GC Found-statusses were cleared")
    }

    private fun handleResult(result:Int, pt:Point) {
        when (result) {
            NO_GEOCACHE -> {
                val msg = "'${pt.name} isn't a geocache, so I can't clear its found state either"
                Logger.logE(TAG, msg)
            }
            STATUS_CLEARED -> {
                val msg = "GC Found-status cleared for ${pt.gcData!!.cacheID}: ${pt.name}"
                Logger.logI(TAG, msg)
                Toast.makeText(this@ActivityClearGCFoundStatus, msg, Toast.LENGTH_SHORT).show()
            }
            STATUS_ALREADY_CLEARED -> {
                val msg = "GC-Found status was already cleared for ${pt.gcData!!.cacheID}: ${pt.name}"
                Logger.logI(TAG, msg)
            }
            ERROR_WHILE_CLEARING_STATUS -> {
                val msg = "Found status NOT cleared for ${pt.name}"
                Toast.makeText(this@ActivityClearGCFoundStatus, "ERROR: " + msg, Toast.LENGTH_SHORT).show()
                Logger.logE(TAG, msg)
            }
            FIELDNOTE_AVAILABLE_STATUS_FIXED-> {
                Toast.makeText(this@ActivityClearGCFoundStatus, "Fieldnote found, marked '${pt.name}' as found", Toast.LENGTH_SHORT).show()
                Logger.logI(TAG, "Fieldnote found in ${pt.gcData!!.cacheID}: ${pt.name}, but cache is not marked as found. Fixed")
            }
            FIELDNOTE_AVAILABLE_STATUS_NOT_FIXED-> Logger.logW(TAG, "Fieldnote found in ${pt.gcData!!.cacheID}: ${pt.name}, but cache is not marked as found. Couldn't fix it. Ah well...");
            FIELDNOTE_AVAILABLE_STATUS_NOT_CLEARED -> {
                val msg = "There's a field note in '${pt.gcData!!.cacheID}: ${pt.name}' so I'm not updating the found status"
                Toast.makeText(this@ActivityClearGCFoundStatus, msg, Toast.LENGTH_SHORT).show()
                Logger.logW(TAG, msg)
            }
        }
    }


    companion object {

        private const val TAG = "ActivityClearGCFoundStatus"
    }
}
