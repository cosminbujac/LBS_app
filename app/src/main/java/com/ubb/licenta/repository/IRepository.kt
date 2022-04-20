package com.ubb.licenta.repository

import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.FirebaseDatabase
import com.ubb.licenta.model.FirebaseMarker
import com.ubb.licenta.utils.Constants.DATABASE_URL

interface IRepository {

    val database: FirebaseDatabase
        get() = FirebaseDatabase.getInstance(DATABASE_URL)

    fun getNearbyMarkers(distance: Double, currentLocation : LatLng)
    fun storeMarker(userID: String, markerOptions: MarkerOptions, imageUri: Uri)
    fun getUserMarkers(userID: String, viewModelCallBack: (FirebaseMarker?) -> Unit)
}