package com.ubb.licenta.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.ubb.licenta.livedata.FirebaseUserLiveData

class LoginViewModel : ViewModel() {


    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED
    }

    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }

}