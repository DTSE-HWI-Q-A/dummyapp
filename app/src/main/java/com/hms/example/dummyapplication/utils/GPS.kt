package com.hms.example.dummyapplication.utils

import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.huawei.hms.common.ApiException
import com.huawei.hms.location.*

public class GPS(val context: Context) : LocationCallback() {
    private val TAG = "GPS Tracker"
    var isStarted:Boolean=false
    var gpsEventListener:OnGPSEventListener? =null


    private val fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun startLocationsRequest(interval: Long =1000) {
        val settingsClient: SettingsClient = LocationServices.getSettingsClient(context)
        val mLocationRequest = LocationRequest()
        // set the interval for location updates, in milliseconds.
        mLocationRequest.interval = interval
        // set the priority of the request
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest)
        val locationSettingsRequest = builder.build()
        // check devices settings before request location updates.
        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener {
                Log.i(TAG, "check location settings success")
                //request location updates
                fusedLocationProviderClient.requestLocationUpdates(
                    mLocationRequest,
                    this,
                    Looper.getMainLooper()
                ).addOnSuccessListener{
                    Log.i(
                        TAG,
                        "requestLocationUpdatesWithCallback onSuccess"
                    )
                }
                    .addOnFailureListener{ e ->
                        Log.e(
                            TAG,
                            "requestLocationUpdatesWithCallback onFailure:" + e.message
                        )
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "checkLocationSetting onFailure:" + e.message)
                val apiException: ApiException = e as ApiException
                when (apiException.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        Log.e(TAG, "Resolution required")
                        gpsEventListener?.onResolutionRequired(e)
                    }
                }
            }
    }

    fun removeLocationUpdatesWithCallback() {
        try {
            fusedLocationProviderClient.removeLocationUpdates(this)
                .addOnSuccessListener{
                    Log.i(
                        TAG,
                        "removeLocationUpdatesWithCallback onSuccess"
                    )
                }
                .addOnFailureListener{ e ->
                    Log.e(
                        TAG,
                        "removeLocationUpdatesWithCallback onFailure:" + e.message
                    )
                }
        } catch (e: Exception) {
            Log.e(TAG, "removeLocationUpdatesWithCallback exception:" + e.message)
        }
    }

    override fun onLocationResult(locationResult: LocationResult?) {
        isStarted=true
        if (locationResult != null) {
            val locations: List<Location> = locationResult.locations
            val lastLocation=locationResult.lastLocation
            gpsEventListener?.onLastKnownLocation(lastLocation.latitude,lastLocation.longitude)
            if (locations.isNotEmpty()) {
                for (location in locations) {
                    Log.e(
                        TAG,
                        "onLocationResult location[Longitude,Latitude,Accuracy]:" + location.getLongitude()
                            .toString() + "," + location.getLatitude()
                            .toString() + "," + location.getAccuracy()
                    )
                }
            }
        }
    }

    interface OnGPSEventListener {
        fun onResolutionRequired(e: Exception)
        fun onLastKnownLocation(lat:Double, lon:Double)
    }
}