package com.deepak.chatapp.view.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.deepak.chatapp.R
import com.deepak.chatapp.view.ui.fragment.MainFragment

const val USER_ID = "uid"
const val USER_NAME = "name"
const val USER_EMAIL = "email"
const val USER_IMAGE = "image"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction().add(R.id.main_frame, MainFragment()).commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
