package com.ubb.licenta.utils

import android.icu.text.DecimalFormat
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil

object MapUtil {
    fun setCameraPosition(location: LatLng): CameraPosition {
        return CameraPosition.Builder()
            .target(location)
            .zoom(18f)
            .build()
    }

    fun calculateElapsedTime(startTime:Long, stopTime:Long):String{
        val elapsedTime = stopTime-startTime
        val seconds = (elapsedTime/1000).toInt() % 60
        val minutes = (elapsedTime/(1000*60)).toInt() % 60
        val hours = (elapsedTime/(1000*60*60)).toInt() % 24


        return "$hours:$minutes:$seconds"
    }

    fun calculateTheDistance(locationList:MutableList<LatLng>):String{
        if(locationList.size>1){
            var meters = 0.0;
            for (i in 1 until locationList.size){
                meters += SphericalUtil.computeDistanceBetween(locationList[i-1], locationList[i])
            }
            val km = meters/1000
            return DecimalFormat("#.##").format(km)
        }
        return "0.00"
    }
}