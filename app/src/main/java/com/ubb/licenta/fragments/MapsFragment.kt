package com.ubb.licenta.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.net.Uri
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider


import com.maps.route.extensions.drawRouteOnMap
import com.maps.route.extensions.moveCameraOnMap
import com.maps.route.model.TravelMode
import com.ubb.licenta.adapters.CustomInfoAdapter
import com.ubb.licenta.R
import com.ubb.licenta.databinding.FragmentMapsBinding
import com.ubb.licenta.model.Result
import com.ubb.licenta.service.TrackerService
import com.ubb.licenta.service.TrackerService.Companion.locationList
import com.ubb.licenta.utils.Constants.ACTION_SERVICE_START
import com.ubb.licenta.utils.Constants.ACTION_SERVICE_STOP
import com.ubb.licenta.utils.MapUtil.calculateElapsedTime
import com.ubb.licenta.utils.MapUtil.calculateTheDistance
import com.ubb.licenta.utils.MapUtil.setCameraPosition
import com.ubb.licenta.viewmodels.MapsViewModel
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
    var drawnTrackedPolyline:Polyline? = null
    private var drawnTrackedRoutes = ArrayList<Polyline>()
    private var myLocation: Location? = null

    private val viewModel by viewModels<MapsViewModel>()

    private var newMarker : Marker? = null;

    private val markersOnMap = ArrayList<Marker>()

    private var trackedLocationList = mutableListOf<LatLng>()

    private val currentUser = FirebaseAuth.getInstance().currentUser?.uid

    private var heatmapOverlay = ArrayList<TileOverlay>()

    private var startTime = 0L
    private var stopTime = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.init(requireContext())
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.tracking = this

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_maps,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
      when(item.itemId)  {
          R.id.menu_walked_routes ->{
              if(drawnTrackedRoutes.isEmpty()){
                  lifecycle.coroutineScope.launch {
                      Log.i("Menu","WalkedRoutes")
                      viewModel.userPolyline.observe(viewLifecycleOwner){
                          drawRoute(it)
                      }
                      viewModel.getUserPolyline(currentUser!!)
                      item.title = "Hide walked paths"
                  }
              }
              else{
                  lifecycle.coroutineScope.launch {
                      drawnTrackedRoutes.forEach {
                          it.remove()
                      }
                      drawnTrackedRoutes.clear()
                      item.title = getString(R.string.show_tracked_paths)
                  }
              }

          }
          R.id.menu_heatmap ->{
              if(heatmapOverlay.isEmpty()){
                  val colors = intArrayOf(
                      Color.rgb(102, 225, 0),  // green
                      Color.rgb(255, 0, 0) // red
                  )
                  val gradient = Gradient(colors,floatArrayOf(0.2f, 1f))
                  viewModel.getHeatmapLatLng(currentUser!!)
                  viewModel.heatmapList.observe(viewLifecycleOwner){
                      val provider = HeatmapTileProvider.Builder()
                          .data(it)
                          .gradient(gradient)
                          .build()

                      val overlay =  map.addTileOverlay(
                          TileOverlayOptions()
                              .tileProvider(provider)
                      )
                      heatmapOverlay.add(overlay!!)
                      showBiggerPicture(it)
                      item.title = "Hide Heatmap"
                  }
              }
              else{
                  heatmapOverlay.forEach {
                      it.remove()
                  }
                  heatmapOverlay.clear()
                  item.title = getString(R.string.show_heatmap)
              }

          }
          R.id.menu_personal_markers ->{
              Log.i("Menu","PersonalMarkers")
              if(markersOnMap.isEmpty()){
                  lifecycle.coroutineScope.launch {
                      item.title = "Hide Personal Markers"
                      viewModel.userMarkers.observe(viewLifecycleOwner){
                          val marker = map.addMarker(it.first)
                          marker!!.tag = it.second
                          markersOnMap.add(marker)
                      }
                      viewModel.providePersonalMarkers(currentUser!!)
                  }
              }
              else{
                  markersOnMap.forEach {
                      it.remove()
                  }
                  markersOnMap.clear()
                  item.title = getString(R.string.show_personal_markers)
                  viewModel.provideCloseMarkers()

              }
          }
          R.id.menu_close_markers ->{
              viewModel.provideCloseMarkers()
          }
          R.id.menu_clean ->{
              map.clear()
              drawnTrackedRoutes.clear()
              markersOnMap.clear()
              heatmapOverlay.clear()
          }
      }
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        newMarkerOptions = MapsFragmentArgs.fromBundle(arguments!!).markerOptions
        newMarkerImageURI = MapsFragmentArgs.fromBundle(arguments!!).markerImageUri
        Log.i("URI",newMarkerImageURI.toString())

        viewModel.closeMarkers.observe(this,androidx.lifecycle.Observer{
            if (this::map.isInitialized){
                val marker = map.addMarker(it.first)
                marker?.tag = it.second.toString()
            }
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
            viewModel.saveMarker(currentUser!!,newMarkerOptions!!,newMarkerImageURI!!)
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
        setOnMapClick(map)
        setOnMarkerClick(map)
        setTracker(map)

        observerTrackerService()
    }

    private fun observerTrackerService(){
        locationList.observe(viewLifecycleOwner) {
            if (it != null) {
                trackedLocationList = it
                drawTrackedRoute()
            }
        }
        TrackerService.startTime.observe(viewLifecycleOwner) {
            startTime = it
        }
        TrackerService.started.observe(viewLifecycleOwner){
            startedTracking.value = it
        }
        TrackerService.stopTime.observe(viewLifecycleOwner) {
            stopTime = it
            if(stopTime!=0L){
                showBiggerPicture(trackedLocationList)
                displayResults()
                saveTrackedRoute()
            }
        }
    }

    private fun displayResults(){
        val result = Result(calculateTheDistance(trackedLocationList),calculateElapsedTime(startTime,stopTime))
        lifecycleScope.launch {
            delay(2500L)
            val directions =  MapsFragmentDirections.actionMapsFragmentToResultFragment(result)
            findNavController().navigate(directions)
        }

    }

    private fun drawTrackedRoute(){
        val oldDrawing = drawnTrackedPolyline
        drawnTrackedPolyline = map.addPolyline(
            PolylineOptions().apply {
                width(10f)
                color(Color.BLUE)
                jointType(JointType.ROUND)
                startCap(ButtCap())
                endCap(ButtCap())
                addAll(trackedLocationList)
            }
        )
        oldDrawing?.remove()
    }

    private fun drawRoute(coordinates: List<LatLng>){
        val polyline = map.addPolyline(
            PolylineOptions().apply {
                width(10f)
                color(Color.GREEN)
                jointType(JointType.ROUND)
                startCap(ButtCap())
                endCap(ButtCap())
                addAll(coordinates)
            }
        )
        drawnTrackedRoutes.add(polyline)
    }



    private fun setTracker(map: GoogleMap) {
        binding.startTrackButton.setOnClickListener{
            drawnTrackedPolyline?.remove()
            sendActionCommandService(ACTION_SERVICE_START)
            disableButton(binding.startTrackButton)
            enableButton(binding.stopTrackButton)
        }
        binding.stopTrackButton.setOnClickListener{
            sendActionCommandService(ACTION_SERVICE_STOP)
            disableButton(binding.stopTrackButton)
            enableButton(binding.startTrackButton)
        }
    }

    private fun saveTrackedRoute() {
        viewModel.savePolyline(trackedLocationList, currentUser!!)
        trackedLocationList.clear()

    }

    private fun showBiggerPicture(boundList : List<LatLng>) {
        val bounds = LatLngBounds.Builder()
        for(location in boundList){
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