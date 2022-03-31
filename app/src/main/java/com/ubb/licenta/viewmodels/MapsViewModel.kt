package com.ubb.licenta.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.ubb.licenta.livedata.FirebaseUserLiveData
import kotlinx.coroutines.launch

class MapsViewModel : ViewModel() {

    private val _myLocation = MutableLiveData<Location>()
    val myLocation get() =_myLocation
    private lateinit var fusedLocationProviderClient : FusedLocationProviderClient

    fun init(context: Context){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        updateMyLocation()
    }

    @SuppressLint("MissingPermission")
    fun updateMyLocation(){
        viewModelScope.launch {
            val location = fusedLocationProviderClient.lastLocation
            location.addOnCompleteListener {
                _myLocation.value = location.result
            }
        }
    }



}