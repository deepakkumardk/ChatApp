package com.deepak.chatapp.view.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.deepak.chatapp.R
import com.deepak.chatapp.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sangcomz.fishbun.FishBun
import com.sangcomz.fishbun.adapter.image.impl.GlideAdapter
import com.sangcomz.fishbun.define.Define
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.collapsing_toolbar.*
import kotlinx.android.synthetic.main.content_profile.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import permissions.dispatcher.NeedsPermission

/**
 * Show the profile of the user including name,email,profile picture and Logout btn
 */
class ProfileActivity : AppCompatActivity() {
    private val auth: FirebaseAuth
            by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore
            by lazy { FirebaseFirestore.getInstance() }
    private val storageRef: StorageReference
            by lazy { FirebaseStorage.getInstance().reference }
    private val sharedPreferences: SharedPreferences?
            by lazy { getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE) }

    private var uid: String? = null
    private var name: String? = null
    private var email: String? = null
    private lateinit var refUser: DocumentReference
    private lateinit var refProfile: StorageReference
    private var photoUri = ArrayList<Uri>()
    private var flagUploaded: Boolean = false   //For Firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        init()
        initToolbar()
        loadUserInfo()
        fab_choose_profile_pic.onClick { pickImageFromGallery() }
        btn_upload.onClick {
            sharedPreferences.set(FLAG_UPLOAD, false)
            uploadProfileImageToStorage()
        }
        btn_logout.onClick {
            alert("You will be logged out!!", "Logout") {
                yesButton { logout() }
                noButton { it.dismiss() }
            }.apply {
                iconResource = R.drawable.ic_logout
            }.show()
        }
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar_collapse)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
    }

    private fun init() {
        val setting = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        firestore.firestoreSettings = setting
    }

    private fun logout() {
        auth.signOut()
        sharedPreferences.set(FLAG_UPLOAD, false)
        finish()
        startActivity(intentFor<MainActivity>().clearTop())
    }

    private fun loadUserInfo() {
        uid = intent?.getStringExtra(USER_ID).toString()
        name = intent?.getStringExtra(USER_NAME).toString()
        email = intent?.getStringExtra(USER_EMAIL).toString()

        flagUploaded = sharedPreferences?.get(FLAG_UPLOAD, false)!!
        refUser = firestore.collection("users").document(uid.toString())
        refProfile = storageRef.child("profile/${uid!!}")

        Glide.with(this)
                .loadImage(R.drawable.ic_person, display_image)

        getImageUrl(object : ImageCallback {
            override fun imageUri(uri: Uri?) {
                Glide.with(this@ProfileActivity)
                        .loadImage(uri!!, display_image, R.drawable.ic_person)
                if (!flagUploaded) {
                    uploadImageUrlToFirestore(uri)
                }
            }
        })
        display_name.text = name
        display_email.text = email
    }

    /**
     * Upload the profile image to the Firebase Storage
     */
    private fun uploadProfileImageToStorage() {
        btn_upload.startAnimation()
//        val compressedUri = photoUri[0].toScaledBitmap(applicationContext)?.toUri()!!
        refProfile.putFile(photoUri[0])
                .addOnCompleteListener {
                    when {
                        it.isSuccessful -> {
//                            btn_upload.doneLoadingAnimation(Color.WHITE, Bitmap.createBitmap())
                            btn_upload.hide()
                            val bytes = it.result?.totalByteCount
                            toast("Profile image uploaded successfully")
                        }
                        else -> toast("Something went wrong...")
                    }
                }
    }

    /**
     * Upload the imageUrl that is fetched from Database storage to the firestore
     * in the "users" collection
     */
    private fun uploadImageUrlToFirestore(uri: Uri) {
        val uriMap = mutableMapOf<String, Any>(USER_IMAGE_URL to uri.toString())
        refUser.set(uriMap, SetOptions.merge())
                .addOnCompleteListener {
                    when {
                        it.isSuccessful -> {
                            sharedPreferences.set(FLAG_UPLOAD, true)
                            log("Profile imageUrl uploaded successfully")
                        }
                        else -> toast("Something went wrong...")
                    }
                }
    }

    /**
     * Fetch the imageUrl from the Database Storage as a Url and load this url into the imageView.
     * Using the shared preference check if the url is uploaded to firestore or not.
     */
    private fun getImageUrl(myCallback: ImageCallback) {
        refProfile.downloadUrl.addOnCompleteListener {
            when {
                it.isSuccessful -> {
                    val profileUrl = it.result
                    myCallback.imageUri(profileUrl)
                }
                else -> toast("Something went wrong...")
            }
        }
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private fun pickImageFromGallery() {
        FishBun.with(this@ProfileActivity)
                .setImageAdapter(GlideAdapter())
                .setMaxCount(1)
                .setMinCount(1)
                .startAlbum()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Define.ALBUM_REQUEST_CODE) {
                photoUri = data?.getParcelableArrayListExtra(Define.INTENT_PATH)!!
//                val scaledBitmap = photoUri[0].toScaledBitmap(applicationContext)
                Glide.with(this)
                        .loadImage(photoUri[0], display_image, R.drawable.ic_person)
                btn_upload.show()
            }
        }
    }
}