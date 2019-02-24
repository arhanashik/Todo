package com.blackspider.todo.app.ui.helper

import android.app.Activity
import com.blackspider.todo.R
import com.blackspider.todo.app.data.local.Const
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.util.*

/*
* This class helps to perform firebase auth using gmail or 'email and password'
* It creates and starts the firebase auth UI
* It also returns the current logged in user anytime
*/
interface AuthHelper {

    // get current logged in user
    fun currentUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }

    // config and start firebase auth UI
    fun signIn(activity: Activity) {
        val providers = Arrays.asList(
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.EmailBuilder().build()
        )

        activity.startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.ic_logo)
                .build(),
            Const.RequestCode.AUTH
        )
    }
}