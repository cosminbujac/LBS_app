package com.ubb.licenta.repository

import android.net.Uri
import android.util.Log
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.freelapp.geofire.asTypedFlow
import com.freelapp.geofire.model.LocationData
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.maps.android.PolyUtil
import com.ubb.licenta.model.FirebaseMarker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.text.SimpleDateFormat
import java.util.*

class FirebaseRepository:IRepository {

    override val database: FirebaseDatabase
        get() = super.database

    var nearbyMarkers: Flow<Map<String, LocationData<FirebaseMarker?>>>? = null

     override fun getNearbyMarkers(distance: Double, currentLocation : LatLng) {
        val geoFire = GeoFire(database.getReference("Locations"))
        val query = geoFire.queryAtLocation(GeoLocation(currentLocation.latitude,currentLocation.longitude),distance)
        nearbyMarkers =
            query
                .asTypedFlow<FirebaseMarker>(database.getReference("Markers"))
                .flowOn(Dispatchers.IO)
    }


     override fun getUserMarkers(userID: String,viewModelCallBack : (FirebaseMarker?) -> Unit){
        database.getReference("Markers").orderByChild("userID").equalTo(userID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var marker:FirebaseMarker? = null
                snapshot.children.forEach {
                    marker = it.getValue(FirebaseMarker::class.java)
                    viewModelCallBack(marker)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("PersonalMarkersRepo",error.toString())
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
               uploadMarker(userID, markerOptions,it)
            }
        }.addOnFailureListener{
            throw Exception("")
        }
    }

    private fun uploadMarker(userID: String,markerOptions: MarkerOptions,imageUri: Uri){
        val firebaseMarker = FirebaseMarker(userID,markerOptions)
        firebaseMarker.imageUrl = imageUri.toString()
        val newReference = database.getReference("Markers").push()
        newReference.setValue(firebaseMarker).addOnSuccessListener {
            Log.i("UploadMarker","Success!")
        }.addOnFailureListener{
            Log.e("UploadMarker","Failure caused by: $it")
        }
        val geoFire = GeoFire(database.getReference("Locations"))
        geoFire.setLocation(newReference.key, GeoLocation(markerOptions.position.latitude,markerOptions.position.longitude))
    }

    suspend fun savePolyline(coordinateList:List<LatLng>, userID: String){
        val dbReference = database.getReference("Polyline").push()
        val encodedPoly = PolyUtil.encode(coordinateList)
        dbReference.child("userID").setValue(userID)
        dbReference.child("polyline").setValue(encodedPoly)
    }

    fun getUserPolyline(userID: String,viewModelCallBack : (String?) -> Unit){
        database.getReference("Polyline").orderByChild("userID").equalTo(userID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val encodedPoly = it.child("polyline").getValue(String::class.java)
                    viewModelCallBack(encodedPoly)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("PersonalMarkersRepo",error.toString())
            }
        })
    }

}