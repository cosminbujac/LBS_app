package com.ubb.licenta.adapters

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.ubb.licenta.R

class MapsBindingAdapter {
    companion object{
        @BindingAdapter("observeTracking")
        @JvmStatic
        fun observeTracking(button: Button, started:Boolean){
            if (started && button.id == R.id.stop_track_button){
                button.visibility = View.VISIBLE
                button.isEnabled = true
            }else if(started && button.id == R.id.start_track_button){
                button.visibility = View.INVISIBLE
                button.isEnabled = false
            }
        }
    }
}