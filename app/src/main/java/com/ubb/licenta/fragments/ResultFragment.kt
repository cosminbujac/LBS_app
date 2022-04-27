package com.ubb.licenta.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ubb.licenta.R
import com.ubb.licenta.databinding.FragmentTrackingResultBinding

class ResultFragment : BottomSheetDialogFragment() {

    private var _binding:FragmentTrackingResultBinding? = null
    private val binding get() = _binding!!

    private val args : ResultFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentTrackingResultBinding.inflate(inflater, container, false)

        binding.distanceValueTextView.text =getString(R.string.time_result,args.result.distance)
        binding.timeValueTextView.text = args.result.time

        binding.shareButton.setOnClickListener {
            dismiss()
        }

        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}