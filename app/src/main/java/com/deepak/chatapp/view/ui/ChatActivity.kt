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
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
    private lateinit var refSenderChats: CollectionReference
    private var userInfo: User? = null
    private lateinit var senderUid: String
    private lateinit var receiverUid: String
    private lateinit var toUserName: String
    private lateinit var toUserEmail: String
    private lateinit var adapter: FirestoreRecyclerAdapter<ChatMessage, ChatViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        senderUid = auth.currentUser?.uid.toString()

        receiverUid = intent?.getStringExtra("toUserUid").toString()
        toUserName = intent?.getStringExtra("toUserName").toString()
        toUserEmail = intent?.getStringExtra("toUserEmail").toString()
        log("$receiverUid $toUserName $toUserEmail")

        supportActionBar?.title = toUserName

        //get all the chat messages from the subcollection myChats mapped to the receiverUid user
        refSenderChats = firestore.collection("chat").document(senderUid)
                .collection("myChats").document(receiverUid)
                .collection("allChats")

        val chatQuery = refSenderChats.orderBy("sentAt", Query.Direction.ASCENDING)

        val options = FirestoreRecyclerOptions.Builder<ChatMessage>()
                .setQuery(chatQuery, ChatMessage::class.java)
                .build()

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
                    senderUid != getItem(position).senderId -> R.layout.item_message_received
                    else -> R.layout.item_message_sent
                }
            }

            override fun onBindViewHolder(holder: ChatViewHolder, position: Int, model: ChatMessage) {
                holder.itemChat.text = model.message
            }

            override fun onChildChanged(type: ChangeEventType, snapshot: DocumentSnapshot, newIndex: Int, oldIndex: Int) {
                super.onChildChanged(type, snapshot, newIndex, oldIndex)
                log("onChildChanged")
                log(snapshot.get("message").toString())
                recycler_view_chat.layoutManager?.scrollToPosition(itemCount - 1)
            }
        }

        recycler_view_chat.apply {
            hasFixedSize()
            layoutManager = LinearLayoutManager(applicationContext)
        }
        recycler_view_chat.adapter = adapter
        adapter.notifyDataSetChanged()

        fab_send.onClick {
            val message = message_edit_text.text.toString()
            if (message.isNotBlank())
                sendMessage(message)
            else
                toast("Message is Blank")
        }

    }

    private fun currentUserInfo(): User? {
        runBlocking {
            async(CommonPool) {
                firestore.collection("users")
                        .document(senderUid)
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
        val chatMapSender = mutableMapOf<String, Any>(
                "message" to message,
                "senderId" to senderUid,
                "receiverId" to receiverUid,
                "sentAt" to Timestamp.now())

        //send message to server and add it to the myChats subcollection
        refSenderChats.document()
                .set(chatMapSender)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        toast("message sent to user")
                        message_edit_text.setText("")
                    } else {
                        toast(task.exception?.message.toString())
                    }
                }

//        val chatMapReceiver = mutableMapOf<String, Any>(
//                "message" to message,
//                "senderId" to receiverUid,
//                "receiverId" to senderUid,
//                "sentAt" to Timestamp.now())

        val refReceiverChats = firestore.collection("chat").document(receiverUid)
                .collection("myChats").document(senderUid)
                .collection("allChats")

        refReceiverChats.document()
                .set(chatMapSender)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        toast("message sent to receiver")
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
        var itemChat = view.find<TextView>(R.id.item_chat_message)
    }
}

fun AppCompatActivity.log(message: String) = Log.d("CHATAPP_DEBUG", message)
