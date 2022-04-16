package com.ubb.licenta.repository

import android.net.Uri
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.FirebaseDatabase
import com.ubb.licenta.utils.Constants.DATABASE_URL

interface IRepository {

    val database: FirebaseDatabase
        get() = FirebaseDatabase.getInstance(DATABASE_URL)

    suspend fun getMarkersTest(){

    }

    suspend fun getCloseMarkers(distance:Double){

    }

    fun getUserMarkers(userID : String){

    }

    fun storeMarker(userID: String, markerOptions: MarkerOptions, imageUri: Uri)
}