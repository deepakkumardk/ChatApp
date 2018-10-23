package com.deepak.chatapp.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.text.format.DateUtils
import android.util.Base64
import android.util.Log
import android.view.View
import com.google.firebase.Timestamp
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.GONE
}

fun FragmentActivity?.replaceFragment(@IdRes id: Int, fragment: Fragment, string: String) {
    this?.supportFragmentManager
            ?.beginTransaction()
            ?.replace(id, fragment)
            ?.addToBackStack(string)
            ?.commit()
}

fun Uri?.toScaledBitmap(context: Context): Bitmap? {
    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, this)
    return Bitmap.createScaledBitmap(bitmap, 720, 720, true)
}

fun Bitmap?.bitmapToString(): String {
    val baos = ByteArrayOutputStream()
    this?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
    val byte = baos.toByteArray()
    return Base64.encodeToString(byte, Base64.DEFAULT)
}

fun Timestamp?.timestampToString(): String {
    val now = System.currentTimeMillis()
    val diff = now - this?.toDate()?.time!!
    Log.d("CHATAPP_DEBUG", diff.toString())
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val year = days / 365
    val yearStr = year.toString()
    return when {
        minutes <= 1 -> "Just now"
        minutes <= 60 -> {
            val ago = DateUtils.getRelativeTimeSpanString(this.toDate().time, now, DateUtils.MINUTE_IN_MILLIS)
            ago.toString()
        }
        hours <= 24 -> {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            sdf.format(this.toDate())
        }
        yearStr == Calendar.YEAR.toString() -> {
            val sdf = SimpleDateFormat("d MMM hh:mm a", Locale.getDefault())
            sdf.format(this.toDate())
        }
        else -> {
            val sdf = SimpleDateFormat("d MMM yy hh:mm a", Locale.getDefault())
            sdf.format(this.toDate())
        }
    }
}
