package com.deepak.chatapp.view.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.deepak.chatapp.R
import com.deepak.chatapp.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sangcomz.fishbun.FishBun
import com.sangcomz.fishbun.adapter.image.impl.GlideAdapter
import com.sangcomz.fishbun.define.Define
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import permissions.dispatcher.NeedsPermission

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
    private var imageUrl: String? = null
    private lateinit var refUser: DocumentReference
    private lateinit var refProfile: StorageReference
    private var photoUri = ArrayList<Uri>()
    private var profileUrl: Uri? = null
    private var flagUploaded: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val setting = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        firestore.firestoreSettings = setting

        loadUserInfo()
        display_image.onClick { pickImageFromGallery() }
        btn_upload.onClick {
            sharedPreferences.set(FLAG_UPLOAD, false)
            uploadProfileImageToStorage()
        }
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
        sharedPreferences.set(FLAG_UPLOAD, false)
        finish()
        startActivity(intentFor<MainActivity>().clearTop())
    }

    private fun loadUserInfo() {
        uid = intent?.getStringExtra(USER_ID).toString()
        name = intent?.getStringExtra(USER_NAME).toString()
        email = intent?.getStringExtra(USER_EMAIL).toString()
        imageUrl = intent?.getStringExtra(USER_IMAGE_URL).toString()

        flagUploaded = sharedPreferences?.get(FLAG_UPLOAD, false)!!
        toast(flagUploaded.toString())
        refUser = firestore.collection("users").document(uid.toString())
        refProfile = storageRef.child("profile/${uid!!}")

        Glide.with(this)
                .loadImage(R.drawable.ic_person, display_image)

        getImageUrl()
        display_name.text = name
        display_email.text = email
    }

    private fun uploadProfileImageToStorage() {
        showProgressBar()
//        val compressedUri = photoUri[0].toScaledBitmap(applicationContext)?.toUri()!!
        refProfile.putFile(photoUri[0])
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        hideProgressBar()
                        toast("Profile image uploaded successfully")
                    } else {
                        toast("Something went wrong...")
                        toast(it.exception?.message.toString())
                        log(it.exception?.message.toString())
                    }
                }
    }

    private fun uploadImageUrlToFirestore(uri: Uri) {
        refUser.update(USER_IMAGE_URL, uri.toString())
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        sharedPreferences.set(FLAG_UPLOAD, false)
                        log("Profile imageUrl uploaded successfully")
                    } else {
                        toast("Something went wrong...")
                        log(it.exception?.message.toString())
                    }
                }
    }

    private fun getImageUrl(): Uri? {
        runBlocking {
            async(CommonPool) {
                refProfile.downloadUrl.addOnCompleteListener {
                    if (it.isSuccessful) {
                        profileUrl = it.result
                        Glide.with(this@ProfileActivity)
                                .loadImage(profileUrl!!, display_image, R.drawable.ic_person)
                        if (!flagUploaded) {
                            uploadImageUrlToFirestore(profileUrl!!)
                        }
                        log(profileUrl.toString())
                    } else {
                        toast(it.exception?.message!!)
                        log(it.exception?.message.toString())
                    }
                }
                return@async profileUrl
            }.await()
        }
        return profileUrl
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

    private fun showProgressBar() {
        progress_bar_profile.show()
        linear_layout_profile.setBackgroundColor(Color.GRAY)
        this.window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun hideProgressBar() {
        progress_bar_profile.hide()
        linear_layout_profile.setBackgroundColor(Color.WHITE)
        this.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
}