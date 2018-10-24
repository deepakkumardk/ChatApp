package com.deepak.chatapp.view.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.deepak.chatapp.R
import com.deepak.chatapp.service.model.User
import com.deepak.chatapp.util.*
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_contacts.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class ContactsActivity : AppCompatActivity() {
    private val auth: FirebaseAuth
            by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore
            by lazy { FirebaseFirestore.getInstance() }
    private lateinit var adapter: FirestoreRecyclerAdapter<User, ContactViewHolder>
    //    private lateinit var refSenderChats: CollectionReference
    private lateinit var uid: String
    private var name: String? = null
    private var email: String? = null
    private var userInfo: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        uid = auth.currentUser?.uid.toString()
        currentUserInfo()

        val refContacts = firestore.collection("contacts").document(uid).collection("myContacts")
        val options = FirestoreRecyclerOptions.Builder<User>()
                .setQuery(refContacts, User::class.java)
                .build()

        adapter = object : FirestoreRecyclerAdapter<User, ContactViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
                return ContactViewHolder(view)
            }

            override fun onBindViewHolder(holder: ContactViewHolder, position: Int, model: User) {
                holder.userName.text = model.name
                holder.userEmail.text = model.email
                val context = holder.itemView.context

                Glide.with(context)
                        .load(model.image)
                        .apply(RequestOptions()
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person)
                                .fitCenter())
                        .into(holder.userImage)
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

        val dividerItemDecoration = DividerItemDecoration(applicationContext, DividerItemDecoration.HORIZONTAL)
        recycler_view_contacts.apply {
            hasFixedSize()
            layoutManager = LinearLayoutManager(applicationContext)
            addItemDecoration(dividerItemDecoration)
        }
        recycler_view_contacts.adapter = adapter
        adapter.notifyDataSetChanged()

        fab.onClick {
            startActivity<AllUsersActivity>(
                    USER_ID to uid,
                    USER_NAME to name,
                    USER_EMAIL to email)
        }

    }

    private fun currentUserInfo(): User? {
        runBlocking {
            async(CommonPool) {
                firestore.collection("users")
                        .document(uid)
                        .get(Source.CACHE)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                userInfo = it.result?.toObject(User::class.java)
                                name = userInfo?.name
                                email = userInfo?.email
                            } else {
                                toast(it.exception?.message.toString())
                            }
                        }
                return@async userInfo
            }.await()
        }
        return userInfo
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            startActivity(intentFor<MainActivity>().clearTop())
        } else {
            adapter.startListening()
        }
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    internal inner class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var userName: TextView = view.find(R.id.item_user_name)
        var userEmail: TextView = view.find(R.id.item_user_email)
        var userImage: CircleImageView = view.find(R.id.item_user_image)
    }
}
