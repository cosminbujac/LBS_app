package com.ubb.licenta.fragments

import android.annotation.SuppressLint
import android.location.Location
import android.net.Uri
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.*


import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.maps.route.extensions.drawRouteOnMap
import com.maps.route.extensions.moveCameraOnMap
import com.maps.route.model.TravelMode
import com.ubb.licenta.adapters.CustomInfoAdapter
import com.ubb.licenta.R
import com.ubb.licenta.databinding.FragmentMapsBinding
import com.ubb.licenta.utils.MapUtil.setCameraPosition
import com.ubb.licenta.viewmodels.MapsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class MapsFragment : Fragment(),OnMapReadyCallback {

    private lateinit var map:GoogleMap
    private var newMarkerOptions : MarkerOptions? = null
    private var newMarkerImageURI : Uri? = null

    private var _binding : FragmentMapsBinding? = null
    private val binding get() =_binding!!

    private var myLocation: Location? = null

    private val viewModel by viewModels<MapsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.init(requireContext())
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        newMarkerOptions = MapsFragmentArgs.fromBundle(arguments!!).markerOptions
        newMarkerImageURI = MapsFragmentArgs.fromBundle(arguments!!).markerImageUri

        mapFragment?.getMapAsync(this)
    }

    @SuppressLint("MissingPermission", "PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        if(newMarkerOptions!=null && newMarkerImageURI!=null){
            val marker = map.addMarker(newMarkerOptions!!)
            marker!!.tag = newMarkerImageURI.toString()
        }
        map.isMyLocationEnabled = true

        viewModel.myLocation.observe(this, androidx.lifecycle.Observer {
            myLocation = viewModel.myLocation.value
        })
        lifecycle.coroutineScope.launch {
            delay(1000)
            if(myLocation!=null){
                map.animateCamera((
                        CameraUpdateFactory.newCameraPosition(
                            setCameraPosition(LatLng(myLocation!!.latitude,myLocation!!.longitude)
                            )
                        )),1000,null)
            }
        }

        map.uiSettings.apply {
            isZoomControlsEnabled = true
            isRotateGesturesEnabled = false
            isTiltGesturesEnabled = false
        }

        setMapLongClick(map)
        map.setInfoWindowAdapter(CustomInfoAdapter(context!!))


        val source = LatLng(46.75781414932984, 23.546505045000536) //starting point (LatLng)
        val destination = LatLng(46.7684769320145, 23.556227256324327) // ending point (LatLng)

        setOnMarkerClick(map)
   //     drawRouteToLocation(source,destination)


    }

    private fun drawRouteToLocation(source:LatLng, destination:LatLng){
        map.run {
            moveCameraOnMap(latLng = source) // if you want to zoom the map to any point

            //Called the drawRouteOnMap extension to draw the polyline/route on google maps
            drawRouteOnMap(
                getString(R.string.google_map_api_key), //your API key
                source = source, // Source from where you want to draw path
                destination = destination, // destination to where you want to draw path
                context = context!!, //Activity context
                travelMode = TravelMode.WALKING
            )
        }
    }


    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            // A snippet is additional text that's displayed after the title.
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            val marker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Add New Marker")
                    .snippet(snippet)
            )
            binding.addMarkerButton.apply {
                visibility = View.VISIBLE
                isEnabled = true
            }
            binding.addMarkerButton.setOnClickListener{
                val action = MapsFragmentDirections.actionMapsFragmentToNewMarkerFragment(marker!!.position)
                findNavController().navigate(action)
                it.visibility = View.INVISIBLE
                it.isEnabled = false
            }
        }
    }

    @SuppressLint("PotentialBehaviorOverride")
    private fun setOnMarkerClick(map:GoogleMap){
        map.setOnMarkerClickListener {
            val marker: Marker = it
            if(!it.title.equals("Add New Marker")){
                binding.goToButton.apply {
                    visibility = View.VISIBLE
                    isEnabled = true
                }
                binding.goToButton.setOnClickListener{
                    lifecycle.coroutineScope.launch {
                        val destination = marker.position
                        viewModel.updateMyLocation()
                        delay(500)
                        val origin = LatLng(myLocation!!.latitude,myLocation!!.longitude)
                        binding.goToButton.apply {
                            visibility = View.INVISIBLE
                            isEnabled = false
                        }
                        drawRouteToLocation(origin,destination)
                    }

                }

            }
            false
        }
    }


    override fun onDestroyView() {

        _binding = null
        super.onDestroyView()
    }
}