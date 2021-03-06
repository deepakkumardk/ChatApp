package com.deepak.chatapp.view.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import org.jetbrains.anko.*

/**
 * Show all the Open Source Libraries used in the app
 */
class LicenseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LicenseActivityUi().setContentView(this)
    }

    class LicenseActivityUi : AnkoComponent<LicenseActivity> {
        private val xmlStyles = { v: Any ->
            when (v) {
                is TextView -> v.textSize = 16f
            }
        }

        override fun createView(ui: AnkoContext<LicenseActivity>) = with(ui) {
            verticalLayout {
                padding = dip(8)
                textView("Kotlin By JetBrains")
                textView("Android Support Libraries AOSP")
                textView("Firebase by Google")
                textView("Anko By Kotlin")
                textView("Glide By Anthony Dekker")
                textView("CircleImageView By Henning Dodenhof")
                textView("Permissions Dispatcher By Shintaro Katafuchi, Marcel Schnelle, Yoshinori Isogai")
                textView("FishBun By Seok-Won Jeong")
                textView("LoadingButtonAndroid By Leandro Borges Ferreira")
                textView("MaterialAboutLibrary Daniel Stoneuk")
            }
        }.applyRecursively(xmlStyles)
    }
}
