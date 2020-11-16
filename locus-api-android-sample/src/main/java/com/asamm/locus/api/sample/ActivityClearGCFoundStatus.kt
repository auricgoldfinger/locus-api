package com.asamm.locus.api.sample

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import locus.api.android.ActionBasics
import locus.api.android.objects.LocusVersion
import locus.api.android.utils.IntentHelper
import locus.api.android.utils.LocusUtils
import locus.api.objects.geoData.Point
import locus.api.utils.Logger

class ActivityClearGCFoundStatus : FragmentActivity() {

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
                    if (pt.gcData!!.isFound!!) {
                        if (clearFoundStatus(pt, lv) == 1) {
                            Toast.makeText(this@ActivityClearGCFoundStatus, "Found status cleared for ${pt.name}", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@ActivityClearGCFoundStatus, "ERROR Found status NOT cleared for ${pt.name}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
//                        Toast.makeText(this@ActivityClearGCFoundStatus, "Force-found of ${pt.name}", Toast.LENGTH_SHORT).show()
//                        pt.gcData!!.isFound = true
//                        ActionBasics.updatePoint(this@ActivityClearGCFoundStatus, lv, pt, true)
                    }
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

    private fun clearFoundStatus(pt: Point, lv: LocusVersion): Int {
        if (pt.gcData != null) {
//        Logger.logD(TAG, "Geocache: " + pt.gcData!!.isAvailable)
//        Logger.logD(TAG, "    type: " + pt.gcData!!.type)

            //                    pt.location.latitude = pt.location.latitude + 0.001
            //                    pt.location.longitude = pt.location.longitude + 0.001

//        pt.gcData!!.encodedHints = "Modified from point"
//        pt.gcData!!.owner = "Auric G"
//        pt.gcData!!.isFound = !pt.gcData!!.isFound;

            pt.gcData!!.isFound = false
//            pt.gcData!!.isAvailable = !pt.gcData!!.isAvailable
//        pt.gcData!!.type = (Math.random() * 13).toInt()
//        Logger.logD(TAG, "Type is now " + pt.gcData!!.type)
            return ActionBasics.updatePoint(this@ActivityClearGCFoundStatus, lv, pt, true)
        } else return 0
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
                    if (clearFoundStatus(pt, lv) == 1) {
                        ++clearCount
                        lastCache = pt.name
                    }
                }
            } catch (e: Exception) {
                Logger.logE(TAG, "loadPointsFromLocus($ptsIds)", e)
            }
        }

        if (clearCount == 1) {
            Toast.makeText(this@ActivityClearGCFoundStatus, "Found status cleared for '$lastCache'", Toast.LENGTH_SHORT).show()
        } else
            Toast.makeText(this@ActivityClearGCFoundStatus, "Found status cleared for for '$lastCache' and  ${clearCount - 1} others", Toast.LENGTH_SHORT).show()
    }


    companion object {

        private const val TAG = "ActivityClearGCFoundStatus"
    }
}
