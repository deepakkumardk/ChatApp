package com.deepak.chatapp.view.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.deepak.chatapp.R
import com.deepak.chatapp.view.ui.fragment.MainFragment
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.anko.startActivity

const val USER_ID = "uid"
const val USER_NAME = "name"
const val USER_EMAIL = "email"

class MainActivity : AppCompatActivity() {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction().add(R.id.main_frame, MainFragment()).commit()
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null)
            startActivity<ContactsActivity>()
    }
}
