package com.tmotions.wms.common

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tmotions.wms.R

object PermissionUtils {
    /**
     * Function to request permission from the user
     */
    fun requestAccessFineLocationPermission(activity: AppCompatActivity, requestId: Int) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            requestId
        )
    }

    /**
     * Function to check if the location permissions are granted or not
     */
    fun isAccessFineLocationGranted(context: Context): Boolean {
        return ContextCompat
            .checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Function to check if location of the device is enabled or not
     */
    fun isLocationEnabled(context: Context): Boolean {
        val locationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Function to show the "enable GPS" Dialog box
     */
    fun showGPSNotEnabledDialog(context: Context,message: String,isFragment: Boolean) {
        AlertDialog.Builder(context)
            .setTitle("Location is disabled")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(context.getString(R.string.enable_now)) { _, _ ->
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton(
                context.getString(R.string.cancel)) { _, _ ->
                if(isFragment)
                    (context as AppCompatActivity).onBackPressed()
                else
                    (context as AppCompatActivity).finish()
            }
            .show()

    }

    @SuppressLint("SuspiciousIndentation")
    fun showLocationEnabledFromSettingDialog(context: Context, isFragment: Boolean) {
        AlertDialog.Builder(context)
            .setTitle("Allow Location Access")
            .setMessage("WMS uses your location to find where are you exactly")
            .setCancelable(false)
            .setPositiveButton("Ok") { _, _ ->
                context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null),
                    ),
                )
            }
            .setNegativeButton(
                context.getString(R.string.cancel)) { _, _ ->
                if(isFragment)
                (context as AppCompatActivity).onBackPressed()
                else
                (context as AppCompatActivity).finish()
            }
            .show()

    }
}