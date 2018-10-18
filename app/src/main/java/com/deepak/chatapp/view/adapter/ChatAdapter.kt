package com.deepak.chatapp.view.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.deepak.chatapp.R
import com.deepak.chatapp.service.model.ChatMessage
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.jetbrains.anko.find

class ChatAdapter(private val options: FirestoreRecyclerOptions<ChatMessage>) : FirestoreRecyclerAdapter<ChatMessage, ChatAdapter.ChatViewHolder>(options) {
    private val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    val query = FirebaseFirestore.getInstance().collection("chat").document(uid).collection("myChats")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_sent, parent, false)
        return ChatViewHolder(view)
    }

    //    TODO("how to get toUid")
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int, model: ChatMessage) {
        query.document("toUid")
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val document = it.result
                        if (document?.exists()!!) {
                            val chats = document.toObject(ChatMessage::class.java)
//                                    holder.chatSender.text = chats?.message
//                                    holder.chatReceiver.text = chats?.message
                        }
                    }
                }
    }

    override fun onDataChanged() {
        super.onDataChanged()
    }


    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var chatSender: TextView = view.find(R.id.item_chat_message)
        var chatReceiver: TextView = view.find(R.id.item_chat_message)
    }
}