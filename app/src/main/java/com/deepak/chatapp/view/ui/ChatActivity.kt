package com.deepak.chatapp.view.ui

import android.content.ClipData
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.Glide
import com.deepak.chatapp.R
import com.deepak.chatapp.service.model.ChatMessage
import com.deepak.chatapp.service.model.User
import com.deepak.chatapp.util.*
import com.firebase.ui.common.ChangeEventType
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.item_message_sent.*
import kotlinx.android.synthetic.main.toolbar_chat.*
import org.jetbrains.anko.clipboardManager
import org.jetbrains.anko.find
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onLongClick
import org.jetbrains.anko.selector
import org.jetbrains.anko.toast

/**
 * The ChatActivity from which the user will chat to the another user
 * using the firestore adapter
 */
class ChatActivity : AppCompatActivity() {
    private val firestore: FirebaseFirestore
            by lazy { FirebaseFirestore.getInstance() }
    private lateinit var refSenderChats: CollectionReference
    private lateinit var refReceiverChats: CollectionReference
    private lateinit var senderUid: String
    private lateinit var receiverUid: String
    private lateinit var toUserName: String
    private lateinit var toUserEmail: String
    private lateinit var adapter: FirestoreRecyclerAdapter<ChatMessage, ChatViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        init()
        initToolbar()
        loadProfileImage()

        val chatOptions = listOf("Copy", "Delete For Me")

        //get all the chat messages from the subcollection myChats mapped to the receiverUid user
        refSenderChats = firestore.collection("chat").document(senderUid)
                .collection("myChats").document(receiverUid)
                .collection("allChats")

        refReceiverChats = firestore.collection("chat").document(receiverUid)
                .collection("myChats").document(senderUid)
                .collection("allChats")

        setMessagesToRead()

        val chatQuery = refSenderChats.orderBy(SENT_AT, Query.Direction.ASCENDING)
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
                holder.itemChatMessage.text = model.message
                holder.itemChatTimestamp.text = model.sentAt?.timestampToString()

                holder.itemChatMessage.onLongClick {
                    selector("Message Options", chatOptions) { _, i ->
                        when (i) {
                            0 -> copyText("Chat Message", model.message)
                            1 -> deleteMessage(model.docId)
                        }
                    }
                }
            }

            override fun onChildChanged(type: ChangeEventType, snapshot: DocumentSnapshot, newIndex: Int, oldIndex: Int) {
                super.onChildChanged(type, snapshot, newIndex, oldIndex)
                log(snapshot.get("message").toString())
                recycler_view_chat.layoutManager?.scrollToPosition(itemCount - 1)
            }
        }

        recycler_view_chat.init(applicationContext)
        recycler_view_chat.adapter = adapter
        adapter.notifyDataSetChanged()

        fab_send.onClick {
            val message = message_edit_text.text.toString()
            when {
                message.isNotBlank() -> {
                    message_edit_text.setText("")
                    Glide.with(this@ChatActivity)
                            .loadImage(R.drawable.ic_not_sent, is_message_sent)
                    sendMessage(message)
                }
                else -> toast("Message is Blank")
            }
        }

    }

    private fun init() {
        val setting = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        firestore.firestoreSettings = setting

        senderUid = intent?.getStringExtra(USER_ID).toString()
        receiverUid = intent?.getStringExtra(TO_USER_ID).toString()
        toUserName = intent?.getStringExtra(TO_USER_NAME).toString()
        toUserEmail = intent?.getStringExtra(TO_USER_EMAIL).toString()
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar_chat)
        toolbar_title.text = toUserName
        supportActionBar?.title = ""
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
    }

    /**
     * Load the profile image into the ImageView in the Toolbar
     */
    private fun loadProfileImage() {
        firestore.collection("users")
                .document(receiverUid)
                .get(Source.CACHE)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val userInfo = it.result?.toObject(User::class.java)
                        user_last_seen.text = userInfo?.isOnline
                        Glide.with(this@ChatActivity)
                                .loadImage(userInfo?.imageUrl!!, user_display_image_chat, R.drawable.ic_person)
                    }
                }
    }

    /**
     * send message to the receiver.
     * The message map will be sent to the both sender and receiver's chat subCollection
     */
    private fun sendMessage(message: String) {
        val docId = refSenderChats.document().id
        val timestamp = Timestamp.now()
        val chatMapSender = mutableMapOf<String, Any>(
                MESSAGE to message,
                SENDER_ID to senderUid,
                RECEIVER_ID to receiverUid,
                IS_READ to false,
                DOC_ID to docId,
                SENT_AT to timestamp)

        //send message to sender and add it to the myChats subcollection
        refSenderChats.document(docId)
                .set(chatMapSender)
                .addOnCompleteListener { task ->
                    when {
                        task.isSuccessful -> {
                            Glide.with(this@ChatActivity)
                                    .loadImage(R.drawable.ic_sent, is_message_sent)
                            log("message sent to user")
                        }
                        else -> toast(task.exception?.message.toString())
                    }
                }

        //send message to receiver and add it to the myChats subcollection
        refReceiverChats.document(docId)
                .set(chatMapSender)
                .addOnCompleteListener { task ->
                    when {
                        task.isSuccessful -> log("message sent to receiver")
                        else -> toast(task.exception?.message.toString())
                    }
                }
        setLastMessageSentAt(timestamp)
    }

    /**
     * LAST_MESSAGE_SENT_AT field in the subCollection myContacts will be used to det. the
     * recent chat and so the contact list can be order by this field
     */
    private fun setLastMessageSentAt(timestamp: Timestamp) {
        val refContacts = firestore.collection("contacts")
                .document(senderUid).collection("myContacts")
        val map = mutableMapOf<String, Any>(LAST_MESSAGE_SENT_AT to timestamp)

        refContacts.document(receiverUid)
                .set(map, SetOptions.merge())
                .addOnCompleteListener {
                    when {
                        it.isSuccessful -> log("set lastMessageSentAt")
                        else -> log(it.exception?.message!!)
                    }
                }
    }

    /**
     * Delete the message only for the current logged in  user
     * The message will be deleted using the DOC_ID field
     */
    private fun deleteMessage(docId: String) {
        refSenderChats.document(docId)
                .delete()
                .addOnCompleteListener { task ->
                    when {
                        task.isSuccessful -> toast("Message deleted Successfully")
                        else -> toast(task.exception?.message!!)
                    }
                }
    }

    /**
     * As the user opens up the ChatActivity Set messages to read so that in the
     * contactsActivity we can count the no. of unread messages and can show it
     * and also send the push notification
     */
    //TODO look at this you have to update again at both sender and receiver side for IS_READ
    private fun setMessagesToRead() {
        refReceiverChats.document()
                .get(Source.CACHE)
                .addOnCompleteListener {
                    when {
                        it.isSuccessful -> {
                            val chat = it.result?.toObject(ChatMessage::class.java)
                            if (chat?.isRead!!) {
                                Glide.with(this@ChatActivity)
                                        .loadImage(R.drawable.ic_read, is_message_sent)
                            }
                            log("All messages read")
                        }
                        else -> log(it.exception?.message!!)
                    }
                }
    }

    private fun copyText(label: String, text: String) {
        clipboardManager.primaryClip = ClipData.newPlainText(label, text)
        toast("Text Copied")
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
        var itemChatMessage = view.find<TextView>(R.id.item_chat_message)
        var itemChatTimestamp = view.find<TextView>(R.id.item_chat_message_timestamp)
    }
}
