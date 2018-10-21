package com.deepak.chatapp.view.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.deepak.chatapp.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_profile.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class ProfileActivity : AppCompatActivity() {
    private val auth: FirebaseAuth
            by lazy { FirebaseAuth.getInstance() }
    private var name: String? = null
    private var email: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        loadUserInfo()

        btn_logout.onClick { _ ->
            alert("You will be logged out!!", "Logout") {
                yesButton { logout() }
                noButton { it.dismiss() }
            }.apply {
                iconResource = R.drawable.ic_logout
            }.show()
        }
    }

    private fun logout() {
        auth.signOut()
        finish()
        startActivity(intentFor<MainActivity>().clearTop())
    }

    private fun loadUserInfo() {
        name = intent?.getStringExtra(USER_NAME).toString()
        email = intent?.getStringExtra(USER_EMAIL).toString()

        display_name.text = name
        display_email.text = email
    }
}
