package com.ubb.licenta.utils

import android.Manifest
import android.content.Context
import androidx.fragment.app.Fragment
import com.ubb.licenta.utils.Constants.PERMISSION_LOCATION_REQUEST_CODE
import com.vmadalin.easypermissions.EasyPermissions

object Permissions {

    fun hasLocationPermission(context: Context) =
        EasyPermissions.hasPermissions(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

    fun requestLocationPermission(fragment:Fragment){
        EasyPermissions.requestPermissions(
            fragment,
            "This app cannot work without location permission!",
            PERMISSION_LOCATION_REQUEST_CODE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

}