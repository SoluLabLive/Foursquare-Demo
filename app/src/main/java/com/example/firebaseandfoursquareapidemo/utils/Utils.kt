package com.example.firebaseandfoursquareapidemo.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.net.NetworkInfo
import android.net.ConnectivityManager



object Utils {

    fun checkPermission(permission: String, context: Context): Boolean {
        val result = ContextCompat.checkSelfPermission(context, permission)
        return result == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Is gps enabled boolean.
     *
     * @param mContext the m context
     * @return the boolean
     */
    fun isGPSEnabled(mContext: Context): Boolean {
        val manager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }
    fun isLocationEnabled(mContext: Context): Boolean {
        val lm = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)
    }
    fun displayPromptForEnablingGPS(con: Context) {
        val builder = AlertDialog.Builder(con)
        val action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
        val message = "Do you want open GPS setting?"

        builder.setMessage(message)
            .setPositiveButton(
                "OK"
            ) { d, id ->
                con.startActivity(Intent(action))
                d.dismiss()
            }
            .setNegativeButton(
                "Cancel"
            ) { d, id -> d.cancel()
                (con as AppCompatActivity).finish()
            }
        builder.create().show()
    }
    fun isConnectedToInternet(context: Context): Boolean {
        val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivity != null) {
            val info = connectivity.allNetworkInfo
            if (info != null)
                for (i in info.indices)
                    if (info[i].state == NetworkInfo.State.CONNECTED) {
                        return true
                    }
        }
        return false
    }
}
