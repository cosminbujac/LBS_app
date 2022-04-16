package com.ubb.licenta.model

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

data class FirebaseMarker (
    var userID: String,
)
{
    var imageUrl:String = ""
    var title:String = ""
    var description:String =""
    var lng:Double =0.toDouble()
    var lat:Double = 0.toDouble()

    constructor(userID: String,markerOptions: MarkerOptions):this(userID){
        title = markerOptions.title.toString()
        description = markerOptions.snippet.toString()
        lng = markerOptions.position.longitude
        lat = markerOptions.position.latitude
    }
    constructor():this("")

    override fun toString(): String {
       return "title:$title, description:$description, userID=$userID";
    }
}
