package com.deepak.chatapp.util

import android.net.Uri
import com.deepak.chatapp.service.model.User

interface ImageCallback {
    fun imageUri(uri: Uri?)
}

interface UserCallback {
    fun userInfo(user: User?)
}