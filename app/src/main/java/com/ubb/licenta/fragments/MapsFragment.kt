package com.ubb.licenta.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.net.Uri
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*


import com.maps.route.extensions.drawRouteOnMap
import com.maps.route.extensions.moveCameraOnMap
import com.maps.route.model.TravelMode
import com.ubb.licenta.adapters.CustomInfoAdapter
import com.ubb.licenta.R
import com.ubb.licenta.databinding.FragmentMapsBinding
import com.ubb.licenta.service.TrackerService
import com.ubb.licenta.utils.Constants.ACTION_SERVICE_START
import com.ubb.licenta.utils.Constants.ACTION_SERVICE_STOP
import com.ubb.licenta.utils.MapUtil.setCameraPosition
import com.ubb.licenta.viewmodels.MapsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


class MapsFragment : Fragment(),OnMapReadyCallback {

    private lateinit var map:GoogleMap
    private var newMarkerOptions : MarkerOptions? = null
    private var newMarkerImageURI : Uri? = null

    private var _binding : FragmentMapsBinding? = null
    private val binding get() =_binding!!

    val startedTracking = MutableLiveData<Boolean>(false)
    private var myLocation: Location? = null

    private val viewModel by viewModels<MapsViewModel>()

    private var newMarker : Marker? = null;

    private var trackingLocationList = mutableListOf<LatLng>()
    private var trackedPolylineList  = mutableListOf<Polyline>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.init(requireContext())
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.tracking = this



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        newMarkerOptions = MapsFragmentArgs.fromBundle(arguments!!).markerOptions
        newMarkerImageURI = MapsFragmentArgs.fromBundle(arguments!!).markerImageUri

        viewModel.closeMarkers.observe(this,androidx.lifecycle.Observer{
            addMarkerOnMap(it)
        })

        mapFragment?.getMapAsync(this)
    }

    @SuppressLint("MissingPermission", "PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setInfoWindowAdapter(CustomInfoAdapter(context!!))

        if(newMarkerOptions!=null && newMarkerImageURI!=null){
            val marker = map.addMarker(newMarkerOptions!!)
            marker!!.tag = newMarkerImageURI.toString()
        }
        map.isMyLocationEnabled = true

        viewModel.myLocation.observe(this, androidx.lifecycle.Observer {
            myLocation = viewModel.myLocation.value
        })

        viewModel.provideCloseMarkers()

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
        setOnMapClick(map)
        setOnMarkerClick(map)
        setTracker(map)

        observerTrackerService()
    }

    private fun observerTrackerService(){
        TrackerService.locationList.observe(viewLifecycleOwner) {
            if (it != null) {
                trackingLocationList = it
                drawTrackedRoute()
            }
        }
        TrackerService.started.observe(viewLifecycleOwner){
            startedTracking.value = it
        }
    }

    private fun drawTrackedRoute(){
        val polyline = map.addPolyline(
            PolylineOptions().apply {
                width(10f)
                color(Color.BLUE)
                jointType(JointType.ROUND)
                startCap(ButtCap())
                endCap(ButtCap())
                addAll(trackingLocationList)
            }
        )

        trackedPolylineList.add(polyline)
    }



    private fun setTracker(map: GoogleMap) {
        binding.startTrackButton.setOnClickListener{
            sendActionCommandService(ACTION_SERVICE_START)
            disableButton(binding.startTrackButton)
            enableButton(binding.stopTrackButton)
        }
        binding.stopTrackButton.setOnClickListener{
            sendActionCommandService(ACTION_SERVICE_STOP)
            showBiggerPicture()
            disableButton(binding.stopTrackButton)
            enableButton(binding.startTrackButton)
        }
    }

    private fun showBiggerPicture() {
        val bounds = LatLngBounds.Builder()
        for(location in trackingLocationList){
            bounds.include(location)
        }
        map.animateCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                100
            ),
            2000,
            null
        )
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
                travelMode = TravelMode.DRIVING
            )
        }
    }


    private fun setMapLongClick(map: GoogleMap) {

        map.setOnMapLongClickListener { latLng ->
            newMarker?.remove()
            // A snippet is additional text that's displayed after the title.
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            newMarker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Add New Marker")
                    .snippet(snippet)
            )
            disableButton(binding.goToButton)
            enableButton(binding.addMarkerButton)
            binding.goToButton
            binding.addMarkerButton.setOnClickListener{
                val action = MapsFragmentDirections.actionMapsFragmentToNewMarkerFragment(newMarker!!.position)
                findNavController().navigate(action)
                disableButton(binding.addMarkerButton)
            }
        }
    }

    private fun setOnMapClick(map:GoogleMap){

        map.setOnMapClickListener {
            newMarker?.remove()
            newMarker = null
            disableButton(binding.addMarkerButton)
            disableButton(binding.goToButton)
        }

    }

    @SuppressLint("PotentialBehaviorOverride")
    private fun setOnMarkerClick(map:GoogleMap){
        map.setOnMarkerClickListener {
            val marker: Marker = it
            if(!it.title.equals("Add New Marker")){
                disableButton(binding.addMarkerButton)
                enableButton(binding.goToButton)
                binding.goToButton.setOnClickListener{
                    lifecycle.coroutineScope.launch {
                        val destination = marker.position
                        viewModel.updateMyLocation()
                        delay(500)
                        val origin = LatLng(myLocation!!.latitude,myLocation!!.longitude)
                        disableButton(binding.goToButton)
                        drawRouteToLocation(origin,destination)
                    }

                }

            }
            false
        }
    }

    private fun addMarkerOnMap(options: MarkerOptions){
        if (this::map.isInitialized){
            map.addMarker(options)
        }
    }

    private fun disableButton(button:Button){
        button.apply{
            visibility = View.INVISIBLE
            isEnabled = false
        }
    }

    private fun enableButton(button:Button){
        button.apply {
            visibility = View.VISIBLE
            isEnabled = true
        }
    }

    private fun sendActionCommandService(action:String){
        Intent(requireContext(),
            TrackerService::class.java
        ).apply {
            this.action = action
            requireContext().startService(this)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}