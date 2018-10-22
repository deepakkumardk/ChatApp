package com.deepak.chatapp.view.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.deepak.chatapp.R
import com.deepak.chatapp.service.model.User
import com.deepak.chatapp.util.bitmapToString
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
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
    private var uid: String? = null
    private var name: String? = null
    private var email: String? = null
    private var image: String? = null
    private var userInfo: User? = null
    private lateinit var refUser: DocumentReference
    private var photoUri = ArrayList<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        loadUserInfo()
        refUser = firestore.collection("users").document(uid.toString())

        btn_logout.onClick { _ ->
            alert("You will be logged out!!", "Logout") {
                yesButton { logout() }
                noButton { it.dismiss() }
            }.apply {
                iconResource = R.drawable.ic_logout
            }.show()
        }
        display_image.onClick { pickImageFromGallery() }
    }

    private fun logout() {
        auth.signOut()
        finish()
        startActivity(intentFor<MainActivity>().clearTop())
    }

    private fun loadUserInfo() {
        uid = intent?.getStringExtra(USER_ID).toString()
        name = intent?.getStringExtra(USER_NAME).toString()
        email = intent?.getStringExtra(USER_EMAIL).toString()

        profileImage()
        display_name.text = name
        display_email.text = email
        Glide.with(this)
                .load(image)
                .apply(RequestOptions()
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .centerCrop())
                .into(display_image)
    }

    private fun profileImage(): User? {
        runBlocking {
            async(CommonPool) {
                firestore.collection("users")
                        .document(uid!!)
                        .get(Source.CACHE)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                userInfo = it.result?.toObject(User::class.java)
                                image = userInfo?.image
                            } else {
                                toast(it.exception?.message.toString())
                            }
                        }
                return@async userInfo
            }.await()
        }
        return userInfo
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
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, photoUri[0])
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 720, 720, true)
                Glide.with(this)
                        .load(scaledBitmap)
                        .apply(RequestOptions()
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person)
                                .fitCenter())
                        .into(display_image)
            }
        }
    }

    private fun uploadImageToFirestore(bitmap: Bitmap) {
        val userMap = mutableMapOf<String, Any>(
                USER_IMAGE to bitmap.bitmapToString())

        refUser.update(USER_IMAGE, bitmap.bitmapToString())
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        toast("Profile image uploaded successfully")
                    } else {
                        toast("Something went wrong...")
                        toast(it.exception?.message.toString())
                    }
                }
    }
}
