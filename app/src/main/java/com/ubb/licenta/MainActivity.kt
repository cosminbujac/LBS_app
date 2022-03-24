package com.ubb.licenta

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContentProviderCompat.requireContext

import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import com.firebase.ui.auth.AuthUI
import com.google.android.material.navigation.NavigationView
import android.content.DialogInterface
import androidx.fragment.app.FragmentManager


class MainActivity : AppCompatActivity() {

    lateinit var toggle : ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val drawerLayout : DrawerLayout = findViewById(R.id.drawerLayout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        navView.menu.findItem(R.id.logoutItem).setOnMenuItemClickListener {
            drawerLayout.closeDrawers()
            val awaitLogout= AuthUI.getInstance().signOut(this)
            awaitLogout.addOnCompleteListener{
                Toast.makeText(this,"LoggedOut!",Toast.LENGTH_SHORT).show()
                navController.navigate(R.id.loginFragment)
            }
            true
        }

        toggle = ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }

}