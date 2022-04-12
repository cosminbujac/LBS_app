package com.ubb.licenta.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.ubb.licenta.utils.Constants.ACTION_SERVICE_START
import com.ubb.licenta.utils.Constants.ACTION_SERVICE_STOP
import com.ubb.licenta.utils.Constants.LOCATION_MAX_UPDATE_INTERVAL
import com.ubb.licenta.utils.Constants.LOCATION_USUAL_UPDATE_INTERVAL
import com.ubb.licenta.utils.Constants.NOTIFICATION_CHANNEL_ID
import com.ubb.licenta.utils.Constants.NOTIFICATION_CHANNEL_NAME
import com.ubb.licenta.utils.Constants.NOTIFICATION_ID
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception
import javax.inject.Inject

@AndroidEntryPoint
class TrackerService : LifecycleService() {

    @Inject
    lateinit var notification:NotificationCompat.Builder

    @Inject
    lateinit var notificationManager:NotificationManager

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val locationCallback = object:LocationCallback(){
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            result.locations.let { locations->
                for(loc in locations){
                    updateLocationList(loc)
                }
            }
        }
    }

    companion object{
        val started = MutableLiveData<Boolean>()
        val locationList = MutableLiveData<MutableList<LatLng>>()
    }

    private fun setInitialValues(){
        started.postValue(true)
        locationList.postValue(mutableListOf())
    }

    override fun onCreate() {
        setInitialValues()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                ACTION_SERVICE_START ->{
                    started.postValue(true)
                    startForegroundService()
                    locationUpdates()
                }
                ACTION_SERVICE_STOP ->{
                    started.postValue(false)
                    stopForegroundService()
                }
                else -> {

                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun stopForegroundService() {
        removeLocationUpdates()
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(
            NOTIFICATION_ID
        )
        stopForeground(true)
        stopSelf()
    }

    private fun removeLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun updateLocationList(loc: Location){
        var lastLocation:LatLng? = null;
        try{
            lastLocation = locationList.value?.last()
        }
        catch (e:Exception){
            lastLocation = LatLng(0.0,0.0)
        }
        val newLatLng = LatLng(loc.latitude,loc.longitude)
        if(newLatLng!= lastLocation){
            locationList.value?.apply {
                add(newLatLng)
                locationList.postValue(this)
            }
        }
    }


    private fun startForegroundService(){
        createNotificationChannel()
        startForeground(
            NOTIFICATION_ID,
            notification.build()
        )
    }

    @SuppressLint("MissingPermission")
    private fun locationUpdates(){
        val locationRequest  = LocationRequest().apply {
            interval = LOCATION_USUAL_UPDATE_INTERVAL
            fastestInterval = LOCATION_MAX_UPDATE_INTERVAL
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun createNotificationChannel(){
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

}