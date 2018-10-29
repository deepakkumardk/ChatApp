package com.deepak.chatapp.view.ui

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.Glide
import com.deepak.chatapp.R
import com.deepak.chatapp.service.model.User
import com.deepak.chatapp.util.*
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.*
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_all_users.*
import kotlinx.android.synthetic.main.toolbar.*
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

/**
 * This activity will show all the users who have registered in the app
 */
class AllUsersActivity : AppCompatActivity() {
    private val firestore: FirebaseFirestore
            by lazy { FirebaseFirestore.getInstance() }
    private lateinit var adapter: FirestoreRecyclerAdapter<User, UserViewHolder>
    private lateinit var uid: String
    private lateinit var refUsers: DocumentReference
    private var name: String? = null
    private var email: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_users)

        init()
        initToolbar()
        val query = firestore.collection("users")
                .orderBy(USER_NAME, Query.Direction.ASCENDING)
        val options = FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User::class.java)
                .build()

        adapter = object : FirestoreRecyclerAdapter<User, UserViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
                return UserViewHolder(view)
            }

            override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: User) {
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
                                Glide.with(this@AllUsersActivity)
                                        .loadImage(url, holder.userImage, R.drawable.ic_person)
                            }
                        }

                holder.itemView.setOnClickListener { _ ->
                    val addUser = User(model.uid, model.name, model.email)
                    if (getItem(position).uid != uid) {
                        addUserToContacts(addUser)

                        val toUserUid = getItem(position).uid
                        val toUserName = getItem(position).name
                        val toUserEmail = getItem(position).email
                        val toUserImageUrl = getItem(position).imageUrl
                        context.startActivity<ChatActivity>(
                                USER_ID to uid,
                                TO_USER_ID to toUserUid,
                                TO_USER_NAME to toUserName,
                                TO_USER_EMAIL to toUserEmail,
                                TO_USER_IMAGE_URL to toUserImageUrl)
                    } else {
                        toast("Hey! it's you")
                    }
                }
            }
        }

        recycler_view_users.init(applicationContext)
        recycler_view_users.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "All Users"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
    }

    private fun init() {
        val setting = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        firestore.firestoreSettings = setting

        uid = intent?.getStringExtra(USER_ID).toString()
        name = intent?.getStringExtra(USER_NAME).toString()
        email = intent?.getStringExtra(USER_EMAIL).toString()
    }

    /**
     * When user click on one of the users then it will be added to their contacts list.
     * But it won't be shown in their contact list until and unless the user send the
     * first message to them
     */
    private fun addUserToContacts(addUser: User) {
        val user = mutableMapOf<String, Any>(
                USER_ID to addUser.uid,
                USER_NAME to addUser.name,
                USER_EMAIL to addUser.email)

        // Add the clicked user to the current user's contact List
        val refContacts = firestore.collection("contacts").document(uid).collection("myContacts")
        refContacts.document(addUser.uid)
                .set(user, SetOptions.merge())
                .addOnCompleteListener { task ->
                    when {
                        task.isSuccessful -> log("User added to contacts")
                        else -> toast("Something went wrong...")
                    }
                }

        val activeUser = mutableMapOf<String, Any>(
                USER_ID to uid,
                USER_NAME to name.toString(),
                USER_EMAIL to email.toString())

        // Add the current user to the clicked user's contact List
        val refContactsReceiver = firestore.collection("contacts").document(addUser.uid).collection("myContacts")
        refContactsReceiver.document(uid)
                .set(activeUser, SetOptions.merge())
                .addOnCompleteListener { task ->
                    when {
                        task.isSuccessful -> log("User added to contacts")
                        else -> toast("Something went wrong...")
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

    internal inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var userName: TextView = view.find(R.id.item_user_name)
        var userEmail: TextView = view.find(R.id.item_user_email)
        var userImage: CircleImageView = view.find(R.id.item_user_image)
    }
}
