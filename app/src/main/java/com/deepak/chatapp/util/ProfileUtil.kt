package com.deepak.chatapp.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.FileOutputStream

object ProfileUtil {
    fun compressImage() {
        val fileOutputStream: FileOutputStream? = null
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ALPHA_8
    }
}