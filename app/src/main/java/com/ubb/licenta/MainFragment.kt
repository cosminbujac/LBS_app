package com.ubb.licenta

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
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
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.ubb.licenta.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    companion object {
        const val TAG = "MainFragment"
        const val SIGN_IN_RESULT_CODE = 1001
    }

    private lateinit var binding: FragmentMainBinding
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

        viewModel.authenticationState.observe(viewLifecycleOwner, Observer { authenticationState ->
            if (authenticationState.equals(LoginViewModel.AuthenticationState.UNAUTHENTICATED)) {
                    Toast.makeText(activity,"Nav activated!",Toast.LENGTH_SHORT).show()
                    val action = MainFragmentDirections.actionMainFragmentToLoginFragment()
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


}