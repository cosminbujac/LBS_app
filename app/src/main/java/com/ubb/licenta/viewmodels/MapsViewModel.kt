package com.ubb.licenta.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.net.Uri
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import com.ubb.licenta.model.MarkerDTO
import com.ubb.licenta.repository.FirebaseRepository
import com.ubb.licenta.repository.IRepository
import com.ubb.licenta.utils.Constants
import com.ubb.licenta.utils.Constants.CLOSE_MARKER_COLOR
import com.ubb.licenta.utils.Constants.PERSONAL_MARKER_COLOR
import com.ubb.licenta.utils.MapUtil
import kotlinx.coroutines.launch

class MapsViewModel : ViewModel() {

    private val _myLocation = MutableLiveData<Location>()
    val myLocation get() =_myLocation

    private lateinit var fusedLocationProviderClient : FusedLocationProviderClient

    private val _closeMarkers = MutableLiveData<Pair<MarkerOptions,Uri>>()
    val closeMarkers get() =_closeMarkers

    private val _userPolyline = MutableLiveData<List<LatLng>>()
    val userPolyline get() =_userPolyline

    private val _userMarkers = MutableLiveData<Pair<MarkerOptions,Uri>>()
    val userMarkers get() =_userMarkers


    private val _heatmapList = MutableLiveData<List<LatLng>>()
    val heatmapList get() =_heatmapList

    private val repository : IRepository = FirebaseRepository()

    @SuppressLint("MissingPermission")
    fun init(context: Context,currentUser: String) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        //get location and get the closest markers to said location
        viewModelScope.launch {
            val location = fusedLocationProviderClient.lastLocation
            location.addOnCompleteListener {
                _myLocation.value = location.result
                provideCloseMarkers(currentUser)
            }
            constantlyUpdateLocation()
        }
    }

    @SuppressLint("MissingPermission")
    fun getMyLocation(){
        viewModelScope.launch {
            val location = fusedLocationProviderClient.lastLocation
            location.addOnCompleteListener {
                _myLocation.value = location.result
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun constantlyUpdateLocation(){
        val locationCallback = object: LocationCallback(){
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                val currentLocation = LatLng(result.lastLocation.latitude,result.lastLocation.longitude)
                val lastLocation = myLocation.value?.let { LatLng(it.latitude, myLocation.value!!.longitude) }
                if(MapUtil.calculateTheDistanceLocationUpdate(lastLocation,currentLocation)>=100.0){
                    myLocation.postValue(result.lastLocation)
                    Log.i("UpdatedLocation",myLocation.value.toString())
                }
            }
        }
        val locationRequest  = LocationRequest().apply {

            interval = Constants.LOCATION_USUAL_UPDATE_INTERVAL
            fastestInterval = Constants.LOCATION_MAX_UPDATE_INTERVAL
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun provideCloseMarkers(userID:String){
        val location = LatLng(myLocation.value!!.latitude,myLocation.value!!.longitude)
        viewModelScope.launch {
            repository.getNearbyMarkers(0.6,location)
            repository.nearbyMarkers?.collect{
                  Log.i("ViewModel", it.toString())
                it.forEach { map->
                    val marker = map.value.data
                   if (marker!!.userID != userID)
                        _closeMarkers.value = transformMarker(marker,CLOSE_MARKER_COLOR)
                }
            }
        }
    }

    fun saveMarker(currentUser: String, newMarkerOptions: MarkerOptions, newMarkerImageURI: Uri) {
            repository.storeMarker(currentUser,newMarkerOptions,newMarkerImageURI)
    }

    fun providePersonalMarkers(userID:String){
        viewModelScope.launch {
            repository.getUserMarkers(userID){
                _userMarkers.value = transformMarker(it, PERSONAL_MARKER_COLOR)
            }
        }
    }

    private fun transformMarker(marker:MarkerDTO?, icon: BitmapDescriptor):Pair<MarkerOptions,Uri>{
        return Pair(MarkerOptions()
            .title(marker?.title)
            .snippet(marker?.description)
            .position(LatLng(marker!!.lat, marker.lng))
            .icon(icon),
            Uri.parse(marker.imageUrl)
        )

    }

    fun savePolyline(coordinateList : List<LatLng>, userID: String){
        viewModelScope.launch {
            repository.savePolyline(coordinateList,userID)
        }
    }

    fun getUserPolyline(userID: String){
        viewModelScope.launch {
            repository.getUserPolyline(userID){
                Log.i("UserPolyline",it!!)
                val decodedPolyline = PolyUtil.decode(it)
                _userPolyline.value = decodedPolyline
            }
        }
    }

    fun getHeatmapLatLng(userID: String){
        viewModelScope.launch {
            repository.getUserHeatmap(userID){snapshot ->
                val latLngList = ArrayList<LatLng>()
                snapshot?.children?.forEach {
                    val encodedPoly = it.child("polyline").getValue(String::class.java)
                    val decodedPolyline = PolyUtil.decode(encodedPoly)
                    latLngList.addAll(decodedPolyline)
                }
                _heatmapList.value = latLngList
            }
        }
    }

}