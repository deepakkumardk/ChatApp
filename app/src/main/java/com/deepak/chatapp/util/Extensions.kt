package com.deepak.chatapp.util

import android.graphics.Bitmap
import android.util.Base64
import android.view.View
import java.io.ByteArrayOutputStream

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.GONE
}

fun Bitmap?.bitmapToString(): String {
    val baos = ByteArrayOutputStream()
    this?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
    val byte = baos.toByteArray()
    return Base64.encodeToString(byte, Base64.DEFAULT)
}
