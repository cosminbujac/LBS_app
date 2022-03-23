package com.ubb.licenta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.google.android.material.navigation.NavigationView
import com.ubb.licenta.databinding.FragmentPermissionBinding

class PermissionFragment : Fragment() {

    companion object {
        const val TAG = "MainFragment"
        const val SIGN_IN_RESULT_CODE = 1001
    }

    private var binding: FragmentPermissionBinding? = null
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_permission, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

        viewModel.authenticationState.observe(viewLifecycleOwner, Observer { authenticationState ->
            if (authenticationState.equals(LoginViewModel.AuthenticationState.UNAUTHENTICATED)) {

                    val action = PermissionFragmentDirections.actionPermissionFragmentToLoginFragment()
                    navController.navigate(action)

            }
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        drawerLayout = activity!!.findViewById(R.id.drawerLayout)
        navView = activity!!.findViewById(R.id.nav_view)
        toggle = ActionBarDrawerToggle(activity,drawerLayout,R.string.open,R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.logoutItem ->{
                    Toast.makeText(activity,"LoggedOut!",Toast.LENGTH_SHORT).show()
                    AuthUI.getInstance().signOut(context!!)
                }
            }
            true
        }

        super.onActivityCreated(savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}