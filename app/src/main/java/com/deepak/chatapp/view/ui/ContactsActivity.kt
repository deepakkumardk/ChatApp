package com.deepak.chatapp.view.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.TextView
import com.bumptech.glide.Glide
import com.deepak.chatapp.R
import com.deepak.chatapp.service.model.User
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_contacts.*
import org.jetbrains.anko.find
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class ContactsActivity : AppCompatActivity() {
    private val auth: FirebaseAuth
            by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore
            by lazy { FirebaseFirestore.getInstance() }
    private lateinit var adapter: FirestoreRecyclerAdapter<User, ContactViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        fab.onClick {
            toast("Will add the functionality to add user to contacts from all users")
        }

        val uid = auth.currentUser?.uid.toString()
        val query = firestore.collection("contacts").document(uid).collection("myContacts")
        val options = FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User::class.java)
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
                        .load(R.drawable.ic_person)
                        .into(holder.userImage)
                val a = holder.adapterPosition
                holder.itemView.setOnClickListener {
                    val intent = Intent(context, ChatActivity::class.java)
//                    intent.putExtra("fromUid",uid)
                    // TODO("add the real toUid")
                    intent.putExtra("toUid", "16Ra5MLjFzNIa2evocONOjZgWVE3")
                    context.startActivity(intent)
                }
            }
        }

        recycler_view_contacts.hasFixedSize()
        recycler_view_contacts.layoutManager = LinearLayoutManager(applicationContext)
        recycler_view_contacts.adapter = adapter
        adapter.notifyDataSetChanged()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.action_all_users -> {
                startActivity<AllUsersActivity>()
                true
            }
            R.id.action_profile -> {
                startActivity<ProfileActivity>()
                true
            }
            else -> super.onOptionsItemSelected(item)
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

    internal inner class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var userName: TextView = view.find(R.id.item_user_name)
        var userEmail: TextView = view.find(R.id.item_user_email)
        var userImage: CircleImageView = view.find(R.id.item_user_image)
    }
}
