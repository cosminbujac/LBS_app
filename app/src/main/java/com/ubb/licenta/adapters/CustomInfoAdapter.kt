package com.ubb.licenta.adapters

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.ubb.licenta.R

class CustomInfoAdapter(context: Context):GoogleMap.InfoWindowAdapter {

    private val contentView = (context as Activity).layoutInflater.inflate(R.layout.custom_info_window,null)
    override fun getInfoContents(marker: Marker): View? {
        if(marker.title.equals("Add New Marker"))
            return null
        renderViews(marker,contentView)
        return contentView
    }

    override fun getInfoWindow(marker: Marker): View? {
        if(marker.title.equals("Add New Marker"))
            return null
        renderViews(marker,contentView)
        return contentView
    }

    private fun renderViews(marker:Marker?,contentView:View){
        val title = marker?.title
        val description = marker?.snippet

        val titleTextView = contentView.findViewById<TextView>(R.id.title_textView)
        titleTextView.text = title

        val descriptionTextView = contentView.findViewById<TextView>(R.id.description_textView)
        descriptionTextView.text = description

        val markerImageView = contentView.findViewById<ImageView>(R.id.marker_imageView)
        markerImageView.setImageURI(Uri.parse(marker!!.tag.toString()))

        //change image view as well

    }
}