package com.ubb.licenta.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.ubb.licenta.livedata.FirebaseUserLiveData
import com.ubb.licenta.model.FirebaseMarker
import com.ubb.licenta.repository.FirebaseRepository
import com.ubb.licenta.repository.IRepository
import com.ubb.licenta.utils.Constants.SAVED_MARKER_COLOR
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MapsViewModel : ViewModel() {

    private val _myLocation = MutableLiveData<Location>()
    val myLocation get() =_myLocation

    private lateinit var fusedLocationProviderClient : FusedLocationProviderClient

    private val _closeMarkers = MutableLiveData<Pair<MarkerOptions,Uri>>()
    val closeMarkers get() =_closeMarkers

    private val _userMarkers = MutableLiveData<Pair<MarkerOptions,Uri>>()
    val userMarkers get() =_userMarkers

    val repository = FirebaseRepository()

    @SuppressLint("MissingPermission")
    fun init(context: Context) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        //get location and get the closest markers to said location
        viewModelScope.launch {
            val location = fusedLocationProviderClient.lastLocation
            location.addOnCompleteListener {
                _myLocation.value = location.result
                provideCloseMarkers()
            }
        }
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
        val location = LatLng(myLocation.value!!.latitude,myLocation.value!!.longitude)
        var markers: List<FirebaseMarker>? = null
        viewModelScope.launch {
            repository.getNearbyMarkers(2.0,location)
            repository.nearbyMarkers?.collect{
                  Log.i("ViewModel", it.toString())
                it.forEach { map->
                    val marker = map.value.data
                    _closeMarkers.value = transformMarker(marker)
                }
            }
            Log.i("ViewModel", markers!!.first().toString())
        }
    }

    fun saveMarker(currentUser: String, newMarkerOptions: MarkerOptions, newMarkerImageURI: Uri) {
            repository.storeMarker(currentUser,newMarkerOptions,newMarkerImageURI)
    }

    fun providePersonalMarkers(userID:String){
        viewModelScope.launch {
            repository.getUserMarkers(userID){
                _userMarkers.value = transformMarker(it)
            }
        }
    }

    private fun transformMarker(marker:FirebaseMarker?):Pair<MarkerOptions,Uri>{
        return Pair(MarkerOptions()
            .title(marker?.title)
            .snippet(marker?.description)
            .position(LatLng(marker!!.lat, marker.lng))
            .icon(SAVED_MARKER_COLOR),
            Uri.parse(marker.imageUrl)
        )

    }


}