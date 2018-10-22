package com.deepak.chatapp.service.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class User(
        @SerializedName("uid")
        @Expose
        var uid: String = "",
        @SerializedName("name")
        @Expose
        var name: String = "",
        @SerializedName("email")
        @Expose
        var email: String = "",
        @SerializedName("image")
        @Expose
        var image: String = ""
)