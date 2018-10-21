package com.deepak.chatapp.service.model

import com.google.firebase.Timestamp
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ChatMessage(
        @SerializedName("message")
        @Expose
        var message: String = "",
        @SerializedName("senderId")
        @Expose
        var senderId: String = "",
        @SerializedName("receiverId")
        @Expose
        var receiverId: String = "",
        @SerializedName("timestamp_message")
        @Expose
        var sentAt: Timestamp? = null
)