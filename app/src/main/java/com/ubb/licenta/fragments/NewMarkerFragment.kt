package com.ubb.licenta.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.ubb.licenta.R
import com.ubb.licenta.databinding.FragmentNewMarkerBinding
import com.ubb.licenta.utils.Constants.IMAGE_REQUEST_CODE
import com.ubb.licenta.utils.Constants.PERSONAL_MARKER_COLOR


class NewMarkerFragment : Fragment() {

    private var _binding :FragmentNewMarkerBinding? = null
    private val binding get() =_binding!!

    private var imageURI:Uri? = null


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
        binding.selectFromFiles.setOnClickListener {
            openGalleryForImage()
        }

        binding.saveMarkerButton.setOnClickListener {
            var message = ""
            val title = binding.addTitleEditText.text.toString()
            val description = binding.addDescriptionEditText.text.toString()

            if(imageURI == null)
                message = message.plus("You need to upload an image!\n")
            if(title.isEmpty() || description.isEmpty()){
                message = message.plus("Fields cannot be empty!")
            }
            if(message.isEmpty()){
                val markerOptions = MarkerOptions()
                    .title(title)
                    .snippet(description)
                    .position(position)
                    .icon(PERSONAL_MARKER_COLOR)
                val action = NewMarkerFragmentDirections.actionNewMarkerFragmentToMapsFragment(markerOptions,imageURI)
                findNavController().navigate(action)
            }
            else
            {
                Toast.makeText(activity,message,Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_REQUEST_CODE){
            imageURI = data?.data
        }
    }
}