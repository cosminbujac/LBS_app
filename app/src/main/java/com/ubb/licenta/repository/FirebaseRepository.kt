package com.ubb.licenta.repository

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.firebase.geofire.*
import com.freelapp.geofire.asTypedFlow
import com.freelapp.geofire.model.LocationData
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.ubb.licenta.model.FirebaseMarker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import java.text.SimpleDateFormat
import java.util.*

class FirebaseRepository:IRepository {

    override val database: FirebaseDatabase
        get() = super.database

    private var _downloadUrl = MutableLiveData<Uri?>()
    val downloadUrl get() = _downloadUrl

    override suspend fun getMarkersTest() {
        val geoFire = GeoFire(database.getReference("Locations"))
        val query = geoFire.queryAtLocation(GeoLocation(46.758214146338597, 23.54403594482969),10.0)
        val nearbyMarkers: Flow<Map<String, LocationData<FirebaseMarker?>>> =
            query
                .asTypedFlow<FirebaseMarker>(database.getReference("Markers"))
                .flowOn(Dispatchers.IO)
                .onEach { map ->
                    map.onEach {
                        val key = it.key
                        val (geoLocation, dataSnapshot) = it.value
                        Log.i("LOCATION",it.toString())
                    }
                }
        nearbyMarkers.collect()
    }

    suspend fun getNearbyMarkers(distance: Double, currentLocation : LatLng) {
        val geoFire = GeoFire(database.getReference("Locations"))
        val query = geoFire.queryAtLocation(GeoLocation(46.758214146338597, 23.54403594482969),10.0)
        val nearbyMarkers: Flow<Map<String, LocationData<FirebaseMarker?>>> =
            query
                .asTypedFlow<FirebaseMarker>(database.getReference("Markers"))
                .flowOn(Dispatchers.IO)
                .onEach { map ->
                    map.onEach {
                        val key = it.key
                        val (geoLocation, dataSnapshot) = it.value
                        val marker = it.value
                        //add clinet-side filter to only get different markers
                        Log.i("LOCATION",it.toString())
                    }
                }
        nearbyMarkers.collect()
    }

    override fun getUserMarkers(userID: String) {
        database.getReference("Markers").orderByChild("userID").equalTo(userID).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var marker:FirebaseMarker? = null
                snapshot.children.forEach {
                    marker = it.getValue(FirebaseMarker::class.java)
                    Log.i("UserMarkers", marker.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.i("UserMarkers",error.toString())
            }

        })
    }

    override fun storeMarker(userID: String, markerOptions: MarkerOptions,imageUri: Uri) {
        val formatter = SimpleDateFormat("dd_MM_yyyy_HH_mm_ss", Locale.getDefault())
        val fileName = formatter.format(Date())
        val storageReference = FirebaseStorage.getInstance().getReference("images/$fileName")
        storageReference.putFile(imageUri).
        addOnSuccessListener {
            storageReference.downloadUrl.addOnSuccessListener{
               uploadMarker(userID, markerOptions,imageUri)
            }
        }.addOnFailureListener{
            _downloadUrl.postValue(null)
        }



    }

    private fun uploadMarker(userID: String,markerOptions: MarkerOptions,imageUri: Uri){
        val firebaseMarker = FirebaseMarker(userID,markerOptions)
        firebaseMarker.imageUrl = imageUri.toString()
        val newReference = database.getReference("Markers").push()
        newReference.setValue(firebaseMarker).addOnSuccessListener {
            println("Success!")
        }.addOnFailureListener{
            println("Failure!")
        }
        val geoFire = GeoFire(database.getReference("Locations"))
        geoFire.setLocation(newReference.key, GeoLocation(markerOptions.position.latitude,markerOptions.position.longitude))
    }
}