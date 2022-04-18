package com.ubb.licenta.adapters

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.Glide.with
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.ubb.licenta.GlideApp
import com.ubb.licenta.GlideApp.with
import com.ubb.licenta.GlideApplication
import com.ubb.licenta.R
import java.util.logging.Handler

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
        GlideApp
            .with(contentView)
            .load(marker!!.tag)
            .listener(MarkerCallback(marker))
            .into(markerImageView)

    }

    class MarkerCallback internal constructor(marker: Marker?) :

        RequestListener<Drawable> {
        var done = false;
        val handler = android.os.Handler()
        var marker: Marker? = null
        private fun onSuccess() {
            if (marker != null && marker!!.isInfoWindowShown) {
                marker!!.hideInfoWindow()
                marker!!.showInfoWindow()
            }
        }

        init {
            this.marker = marker
        }

        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            Log.e(javaClass.simpleName, "Error loading thumbnail! -> $e")
            return false
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            if(!done){
                handler.post(kotlinx.coroutines.Runnable {
                    onSuccess()
                })
                done =true;
            }

            return false
        }
    }
}