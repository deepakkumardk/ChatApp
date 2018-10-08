package com.deepak.chatapp.view.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.deepak.chatapp.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_profile.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity

class ProfileActivity : AppCompatActivity() {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        btn_logout.onClick {
            auth.signOut()
            startActivity<MainActivity>()
        }
    }
}
