package com.deepak.chatapp.view.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.deepak.chatapp.R
import com.deepak.chatapp.service.model.ChatMessage
import com.deepak.chatapp.service.model.User
import com.firebase.ui.common.ChangeEventType
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.anko.find
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.toast

class ChatActivity : AppCompatActivity() {
    private val auth: FirebaseAuth
            by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore
            by lazy { FirebaseFirestore.getInstance() }
    private var userInfo: User? = null
    private lateinit var uid: String
    private lateinit var toUid: String
    private lateinit var adapter: FirestoreRecyclerAdapter<ChatMessage, ChatViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // get the all chat messages from the subcollection myChats mapped to the toUid user
//        val currentUser = currentUserInfo()
        uid = auth.currentUser?.uid.toString()

        toUid = intent.getStringExtra("toUid")

        val query = firestore.collection("chat").document(uid)
                .collection("myChats").document(toUid)
                .collection("allChats")

        val options = FirestoreRecyclerOptions.Builder<ChatMessage>()
                .setQuery(query, ChatMessage::class.java)
                .build()

        query.document(toUid)
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val document = it.result
                        if (document?.exists()!!) {
//                            for (document in ) {
                            val chats = document.toObject(ChatMessage::class.java)
                            log("${chats?.message}")
//                            }
                        }
                    } else {
                        toast(it.exception?.message.toString())
                    }
                }

        adapter = object : FirestoreRecyclerAdapter<ChatMessage, ChatViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
                return if (viewType == R.layout.item_message_sent) {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_sent, parent, false)
                    ChatViewHolder(view)
                } else {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_received, parent, false)
                    ChatViewHolder(view)
                }
            }

            override fun getItemViewType(position: Int): Int {
                return when {
                    uid != getItem(position).senderId -> R.layout.item_message_sent
                    else -> R.layout.item_message_received
                }
            }

            override fun onBindViewHolder(holder: ChatViewHolder, position: Int, model: ChatMessage) {
                query.document(toUid)
                        .get()
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                val document = it.result
                                if (document?.exists()!!) {
                                    val chats = document.toObject(ChatMessage::class.java)
//                                    holder.chatSender.text = chats?.message
//                                    holder.chatReceiver.text = chats?.message
                                }
                            } else {
                                toast(it.exception?.message.toString())
                            }
                        }
            }

            override fun onChildChanged(type: ChangeEventType, snapshot: DocumentSnapshot, newIndex: Int, oldIndex: Int) {
                super.onChildChanged(type, snapshot, newIndex, oldIndex)
                toast("onChildChanged")
            }
        }


        recycler_view_chat.hasFixedSize()
        recycler_view_chat.layoutManager = LinearLayoutManager(applicationContext)
        recycler_view_chat.adapter = adapter
        adapter.notifyDataSetChanged()

        fab_send.onClick {
            val message = message_edit_text.text.toString()
            if (message.isNotBlank())
                sendMessage(message)
        }

    }

    private fun currentUserInfo(): User? {
        runBlocking {
            async(CommonPool) {
                firestore.collection("users")
                        .document(uid)
                        .get()
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                userInfo = it.result?.toObject(User::class.java)
                            } else {
                                toast(it.exception?.message.toString())
                            }
                        }
                return@async userInfo
            }.await()
        }
        return userInfo
    }

    private fun sendMessage(message: String) {
        val chatMap = mutableMapOf<String, Any>(
                "message" to message,
//                "senderId" to uid,
                "sendAt" to Timestamp.now())

        //send message to server and add it to the myChats subcollection
//        val docRef = firestore.collection("chat").document(uid).collection("myChats")

        //chat of uid with toUid
        val docRef = firestore.collection("chat").document(uid)
                .collection("myChats").document(toUid)
                .collection("allChats")
        docRef.document()
                .set(chatMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        toast("message sent to user")
                        message_edit_text.setText("")
                    } else {
                        toast(task.exception?.message.toString())
                    }
                }
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    internal inner class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var chatSender: TextView = view.find(R.id.item_chat_sent)
        var chatReceiver: TextView = view.find(R.id.item_chat_received)
    }
}

fun AppCompatActivity.log(message: String) = Log.d("CHATAPP_DEBUG", message)
