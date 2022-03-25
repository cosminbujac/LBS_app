package com.ubb.licenta.fragments

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.ubb.licenta.adapters.CustomInfoAdapter
import com.ubb.licenta.R
import com.ubb.licenta.databinding.FragmentMapsBinding
import com.ubb.licenta.utils.MapUtil.setCameraPosition
import java.util.*

class MapsFragment : Fragment(),OnMapReadyCallback {

    private lateinit var map:GoogleMap
    private lateinit var fusedLocationProviderClinet : FusedLocationProviderClient

    private var newMarkerOptions : MarkerOptions? = null

    private var _binding : FragmentMapsBinding? = null
    private val binding get() =_binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fusedLocationProviderClinet = LocationServices.getFusedLocationProviderClient(requireActivity())

        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        newMarkerOptions = MapsFragmentArgs.fromBundle(arguments!!).markerOptions

        mapFragment?.getMapAsync(this)
    }

    @SuppressLint("MissingPermission", "PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        if(newMarkerOptions!=null)
            map.addMarker(newMarkerOptions!!)
        map.isMyLocationEnabled = true
        val myLocation = fusedLocationProviderClinet.lastLocation
        myLocation.addOnCompleteListener{
            map.animateCamera((
                    CameraUpdateFactory.newCameraPosition(
                        setCameraPosition(LatLng(it.result.latitude,it.result.longitude)
                        )
                    )),1000,null)
        }

        map.uiSettings.apply {
            isZoomControlsEnabled = true
            isRotateGesturesEnabled = false
            isTiltGesturesEnabled = false
        }

        setMapLongClick(map)
        map.setInfoWindowAdapter(CustomInfoAdapter(context!!))
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

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}