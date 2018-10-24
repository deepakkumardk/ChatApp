package com.deepak.chatapp.view.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.deepak.chatapp.R
import com.deepak.chatapp.view.ui.fragment.MainFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction()
                .add(R.id.main_frame, MainFragment()).commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}

fun log(message: String) = Log.d("CHATAPP_DEBUG", message)