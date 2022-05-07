package com.ubb.licenta.repository

import android.net.Uri
import com.freelapp.geofire.model.LocationData
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.ubb.licenta.model.MarkerDTO
import com.ubb.licenta.utils.Constants.DATABASE_URL
import kotlinx.coroutines.flow.Flow

interface IRepository {

    val database: FirebaseDatabase
        get() = FirebaseDatabase.getInstance(DATABASE_URL)

    var nearbyMarkers: Flow<Map<String, LocationData<MarkerDTO?>>>?

    fun getNearbyMarkers(distance: Double, currentLocation : LatLng)
    fun storeMarker(userID: String, markerOptions: MarkerOptions, imageUri: Uri)
    fun getUserMarkers(userID: String, viewModelCallBack: (MarkerDTO?) -> Unit)

    fun savePolyline(coordinateList:List<LatLng>, userID: String)
    fun getUserPolyline(userID: String,viewModelCallBack : (String?) -> Unit)
    fun getUserHeatmap(userID: String,viewModelCallBack : (DataSnapshot?) -> Unit)
}