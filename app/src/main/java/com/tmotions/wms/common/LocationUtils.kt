package com.tmotions.wms.common

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.tmotions.wms.listners.LocationInterface
import java.util.*


class LocationUtils {


    companion object {

        private val TAG: String = "LocationService"

        private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
        private val INTERVAL: Long = 1000
        private val FASTEST_INTERVAL: Long = 1000
        lateinit var mLastLocation: Location
        internal lateinit var mLocationRequest: LocationRequest
        var userCountry = ""

        fun displayLocationSettingsRequest(context: Context, activity: Activity) {
            val googleApiClient = GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build()
            googleApiClient.connect()
            val locationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.interval = 10000
            locationRequest.fastestInterval = 10000 / 2.toLong()
            val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            builder.setAlwaysShow(true)
            val result: PendingResult<LocationSettingsResult> =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
            result.setResultCallback { result ->
                val status: Status = result.status
                when (status.statusCode) {
                    LocationSettingsStatusCodes.SUCCESS -> {
                        Log.i("SelectLocation", "All location settings are satisfied.")
                        userCountry = "aa"
                        getLocationData()
                    }
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        requestDeviceLocationSettings()
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> Log.i(
                        "SelectLocation",
                        "Location settings are inadequate, and cannot be fixed here. Dialog not created."
                    )
                    LocationSettingsStatusCodes.CANCELED -> {
                        Log.i(
                            "SelectLocation",
                            "Cancelled"
                        )
                        Toast.makeText(
                            context, "Location permission denied.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        fun getLocationData() {
            mLocationRequest = LocationRequest()
            startLocationUpdates()
        }

        private fun startLocationUpdates() {
            mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            mLocationRequest.interval = INTERVAL
            mLocationRequest.fastestInterval = FASTEST_INTERVAL

            val builder = LocationSettingsRequest.Builder()
            builder.addLocationRequest(mLocationRequest)
            val locationSettingsRequest = builder.build()

            val settingsClient =
                LocationServices.getSettingsClient(MyApplication.instance.activity)
            settingsClient.checkLocationSettings(locationSettingsRequest)

            mFusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(MyApplication.instance.activity)

            if (ActivityCompat.checkSelfPermission(
                    MyApplication.instance.activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    MyApplication.instance.activity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            Looper.myLooper()?.let {
                mFusedLocationProviderClient!!.requestLocationUpdates(
                    mLocationRequest, mLocationCallback,
                    it
                )
            }
        }

        private val mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {

                locationResult.lastLocation
                onLocationChanged(locationResult.lastLocation)
            }
        }

        fun onLocationChanged(location: Location) {

            mLastLocation = location
            val geocoder = Geocoder(MyApplication.instance, Locale.getDefault())
            val list: List<Address> = geocoder.getFromLocation(mLastLocation.latitude,mLastLocation.longitude!!,1) as List<Address>

            userCountry = list[0].countryName
            Log.d(TAG, userCountry)
            mFusedLocationProviderClient?.removeLocationUpdates(mLocationCallback)
        }

        fun requestDeviceLocationSettings() {
            val locationRequest = LocationRequest.create().apply {
                interval = 10000
                fastestInterval = 5000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

            val client: SettingsClient = LocationServices.getSettingsClient(MyApplication.instance)
            val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

            task.addOnSuccessListener { locationSettingsResponse ->
                // All location settings are satisfied. The client can initialize
                // location requests here.
                userCountry = "aa"
                getLocationData()
            }

            task.addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // By showing the user a dialog.
                    try {
                        // Show the dialog By calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        exception.startResolutionForResult(
                            MyApplication.instance.activity,
                            100
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        //  Ignore the error.
                        //   textView.text = sendEx.message.toString()
                        Toast.makeText(MyApplication.instance, sendEx.message.toString(), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

    }

}

