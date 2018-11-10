package com.deepak.chatapp.view.ui

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.TextView
import com.bumptech.glide.Glide
import com.deepak.chatapp.R
import com.deepak.chatapp.service.model.User
import com.deepak.chatapp.util.*
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_contacts.*
import kotlinx.android.synthetic.main.toolbar.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

/**
 * This activity will show all the contacts of the user
 */
class ContactsActivity : AppCompatActivity() {
    private val auth: FirebaseAuth
            by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore
            by lazy { FirebaseFirestore.getInstance() }

    private lateinit var contactAdapter: FirestoreRecyclerAdapter<User, ContactViewHolder>
    private lateinit var refUsers: DocumentReference
    private lateinit var uid: String
    private var name: String? = null
    private var email: String? = null
    private var imageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        init()
        initToolbar()
        loadUserInfo()
        setOnlineField()

        val contactsQuery = firestore.collection("contacts")
                .document(uid).collection("myContacts")
                .orderBy(LAST_MESSAGE_SENT_AT, Query.Direction.DESCENDING)
        val options = FirestoreRecyclerOptions.Builder<User>()
                .setQuery(contactsQuery, User::class.java)
                .build()

        contactAdapter = object : FirestoreRecyclerAdapter<User, ContactViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
                return ContactViewHolder(view)
            }

            override fun onBindViewHolder(holder: ContactViewHolder, position: Int, model: User) {
                holder.userName.text = model.name
                holder.userEmail.text = model.email
                val context = holder.itemView.context

                //get the images of all users from imageUrl field
                refUsers = firestore.collection("users").document(model.uid)

                refUsers.get(Source.CACHE)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                val itemUser = it.result?.toObject(User::class.java)
                                val url = Uri.parse(itemUser?.imageUrl)
                                Glide.with(this@ContactsActivity)
                                        .loadImage(url, holder.userImage, R.drawable.ic_person)
                            } else {
                                log(it.exception?.message!!)
                            }
                        }

                holder.itemView.setOnClickListener {
                    val toUserUid = getItem(position).uid
                    val toUserName = getItem(position).name
                    val toUserEmail = getItem(position).email
                    context.startActivity<ChatActivity>(
                            USER_ID to uid,
                            TO_USER_ID to toUserUid,
                            TO_USER_NAME to toUserName,
                            TO_USER_EMAIL to toUserEmail)
                }
            }
        }

        recycler_view_contacts.init(applicationContext)
        recycler_view_contacts.adapter = contactAdapter
        contactAdapter.notifyDataSetChanged()

        fab.onClick {
            startActivity<AllUsersActivity>(
                    USER_ID to uid,
                    USER_NAME to name,
                    USER_EMAIL to email)
        }

    }

    private fun init() {
        val setting = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        firestore.firestoreSettings = setting

        uid = auth.currentUser?.uid.toString()
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Contacts"
    }

    /**
     * Set the IS_ONLINE field to Online as the users start the app
     * to show that the user is online or not
     */
    private fun setOnlineField() {
        val isOnline = mutableMapOf<String, Any>(IS_ONLINE to "Online")
        firestore.collection("users").document(uid)
                .set(isOnline, SetOptions.merge())
                .addOnCompleteListener {
                    when {
                        it.isSuccessful -> log("User is Online")
                        else -> log(it.exception?.message!!)
                    }
                }
    }

    /**
     * Get all the fields from firestore of current logged in user from the "users" collection
     */
    private fun loadUserInfo() {
        fetchUserInfo(object : UserCallback {
            override fun userInfo(user: User?) {
                name = user?.name
                email = user?.email
            }
        })
    }

    private fun fetchUserInfo(userCallback: UserCallback) {
        firestore.collection("users")
                .document(uid)
                .get(Source.CACHE)
                .addOnCompleteListener {
                    when {
                        it.isSuccessful -> {
                            val userInfo = it.result?.toObject(User::class.java)
                            userCallback.userInfo(userInfo)
                        }
                        else -> toast(it.exception?.message.toString())
                    }
                }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.action_profile -> {
                startActivity<ProfileActivity>(
                        USER_ID to uid,
                        USER_NAME to name,
                        USER_EMAIL to email)
                true
            }
            R.id.action_about -> {
                startActivity<AboutActivity>()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            startActivity(intentFor<MainActivity>().clearTop())
        } else {
            contactAdapter.startListening()
        }
    }

    override fun onStop() {
        super.onStop()
        contactAdapter.stopListening()
    }

    /**
     * Set the IS_ONLINE field to offline as the users start the app
     * to show that the user is online or not
     */
    override fun onDestroy() {
        val isOnline = mutableMapOf<String, Any>(IS_ONLINE to "Offline")
        firestore.collection("users").document(uid)
                .set(isOnline, SetOptions.merge())
                .addOnCompleteListener {
                    when {
                        it.isSuccessful -> log("User is Offline")
                        else -> log(it.exception?.message!!)
                    }
                }
        super.onDestroy()
    }

    internal inner class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var userName: TextView = view.find(R.id.item_user_name)
        var userEmail: TextView = view.find(R.id.item_user_email)
        var userImage: CircleImageView = view.find(R.id.item_user_image)
    }
}
