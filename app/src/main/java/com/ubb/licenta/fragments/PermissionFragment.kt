package com.ubb.licenta.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.navigation.NavigationView
import com.ubb.licenta.R
import com.ubb.licenta.databinding.FragmentPermissionBinding
import com.ubb.licenta.utils.Permissions.hasLocationPermission
import com.ubb.licenta.utils.Permissions.requestLocationPermission
import com.ubb.licenta.viewmodels.LoginViewModel
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog

class PermissionFragment : Fragment(),EasyPermissions.PermissionCallbacks {

    companion object {
        const val TAG = "MainFragment"
        const val SIGN_IN_RESULT_CODE = 1001
    }

    private var _binding: FragmentPermissionBinding? = null
    private val binding get()  = _binding!!
    lateinit var toggle : ActionBarDrawerToggle
    lateinit var drawerLayout : DrawerLayout
    lateinit var navView : NavigationView
    private lateinit var navController: NavController
    private val viewModel by viewModels<LoginViewModel>()



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_permission, container, false)
        navController = findNavController()
        if (hasLocationPermission(requireContext())){
            val action = R.id.action_permissionFragment_to_mapsFragment
            navController.navigate(action)
        }
        binding.continueButton.setOnClickListener {
            if (hasLocationPermission(requireContext())){
                val action = R.id.action_permissionFragment_to_mapsFragment
                navController.navigate(action)
            }
            else{
                requestLocationPermission(this)
            }
        }

        return binding.root
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this,perms[0])){
            SettingsDialog.Builder(requireActivity()).build().show()
        }
        else{
            requestLocationPermission(this)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        navController.navigate((R.id.action_permissionFragment_to_mapsFragment))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}