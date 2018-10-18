package com.deepak.chatapp.view.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.deepak.chatapp.R
import com.deepak.chatapp.service.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast

class ProfileActivity : AppCompatActivity() {
    private val auth: FirebaseAuth
            by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore
            by lazy { FirebaseFirestore.getInstance() }
    private var userInfo: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        loadUserInfo()

        btn_logout.onClick {
            auth.signOut()
            startActivity(intentFor<MainActivity>().clearTop())
        }
    }

    private fun loadUserInfo(): User? {
        val currentUser: FirebaseUser? = auth.currentUser

        runBlocking {
            async(CommonPool) {
                firestore.collection("users")
                        .document(currentUser?.uid.toString())
                        .get(Source.CACHE)
                        .addOnSuccessListener {
                            userInfo = it.toObject(User::class.java)
                            log("User profile info")
                            log(userInfo?.name.toString())
                            log(userInfo?.email.toString())

                            display_name.text = userInfo?.name
                            display_email.text = userInfo?.email
                            log("User information fetched from firestore")
                        }
                        .addOnFailureListener {
                            toast(it.message.toString())
                            log(it.message.toString())
                        }
                return@async userInfo
            }.await()
        }

        return userInfo
    }
}
