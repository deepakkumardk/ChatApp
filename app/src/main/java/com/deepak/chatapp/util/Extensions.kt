package com.deepak.chatapp.util

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.support.annotation.DrawableRes
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.Timestamp
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Extension functions Super Kotlin
 */
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

fun RecyclerView.init(context: Context) {
    this.apply {
        hasFixedSize()
        layoutManager = LinearLayoutManager(context)
    }
}

fun SharedPreferences?.set(key: String, value: Any) {
    val editor = this?.edit()
    when (value) {
        is Boolean -> editor?.putBoolean(key, value)
        is Int -> editor?.putInt(key, value)
        is Long -> editor?.putLong(key, value)
        is String -> editor?.putString(key, value)
        else -> throw UnsupportedOperationException("Not yet implemented")
    }
    editor?.apply()
}

inline fun <reified T : Any> SharedPreferences?.get(key: String, defValue: Any): T? {
    return when (defValue) {
        is Boolean -> this?.getBoolean(key, defValue) as T
        is Int -> this?.getInt(key, defValue) as T
        is Long -> this?.getLong(key, defValue) as T
        is String -> this?.getString(key, defValue) as T
        else -> throw UnsupportedOperationException("Not yet implemented")
    }
}

fun RequestManager.loadImage(model: Any, view: ImageView) {
    this.load(model)
            .apply(RequestOptions().fitCenter())
            .into(view)
}

fun RequestManager.loadImage(model: Any, view: ImageView,
                             @DrawableRes resId: Int) {
    this.load(model)
            .apply(RequestOptions()
                    .placeholder(resId)
                    .error(resId)
                    .fitCenter())
            .into(view)
}

fun Uri?.toScaledBitmap(context: Context): Bitmap? {
    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, this)
    return Bitmap.createScaledBitmap(bitmap, 720, 720, true)
}

fun Bitmap?.toUri(): Uri? {
    return Uri.parse(this.bitmapToString())
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
