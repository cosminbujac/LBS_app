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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.ubb.licenta.livedata.FirebaseUserLiveData
import kotlinx.coroutines.launch

class MapsViewModel : ViewModel() {

    private val _myLocation = MutableLiveData<Location>()
    val myLocation get() =_myLocation

    private lateinit var fusedLocationProviderClient : FusedLocationProviderClient

    private val _closeMarkers = MutableLiveData<MarkerOptions>()
    val closeMarkers get() =_closeMarkers

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

    fun provideCloseMarkers(){
        val latLongs = listOf<LatLng>(LatLng(46.7597302035116, 23.546723965246155),
            LatLng(46.7564961554647, 23.54638064250526),
            LatLng(46.755320089867475, 23.54552233565302),
            LatLng(46.75955380589592, 23.548526409635866),
            LatLng(46.763633160999966, 23.547659446214848),
            LatLng(46.764209991752594, 23.552354687446336),
            LatLng(46.75810925680717, 23.553145733523383),
            LatLng(46.76340592294707, 23.56008652490906),
            LatLng(46.758214146338595, 23.54403594482967),
            )
        for ((i, latLong) in latLongs.withIndex()) {
            _closeMarkers.value=MarkerOptions()
                .position(latLong)
                .title("title $i")
                .snippet("Description $i")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        }
    }



}