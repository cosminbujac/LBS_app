package com.ubb.licenta.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.ubb.licenta.R
import com.ubb.licenta.databinding.FragmentNewMarkerBinding



class NewMarkerFragment : Fragment() {

    private var _binding :FragmentNewMarkerBinding? = null
    private val binding get() =_binding!!



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewMarkerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val position = NewMarkerFragmentArgs.fromBundle(arguments!!).latLng

        binding.saveMarkerButton.setOnClickListener {

            val title = binding.addTitleEditText.text.toString()
            val description = binding.addDescriptionEditText.text.toString()
            if(title.isEmpty() || description.isEmpty()){
                Toast.makeText(activity,"Fields cannot be empty!",Toast.LENGTH_LONG).show()
                val action = NewMarkerFragmentDirections.actionNewMarkerFragmentToMapsFragment(null)
                findNavController().navigate(action)
            }
            else{
                val markerOptions = MarkerOptions()
                    .title(title)
                    .snippet(description)
                    .position(position)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                val action = NewMarkerFragmentDirections.actionNewMarkerFragmentToMapsFragment(markerOptions)
                findNavController().navigate(action)
            }
        }
    }
}