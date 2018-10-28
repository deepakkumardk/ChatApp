package com.deepak.chatapp.view.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.deepak.chatapp.R
import com.deepak.chatapp.util.*
import com.deepak.chatapp.view.ui.ContactsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_signup.*
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.yesButton

/**
 * The signup interface for the user
 */
class SignupFragment : Fragment() {
    private val auth: FirebaseAuth
            by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore
            by lazy { FirebaseFirestore.getInstance() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        login_text_intent.onClick {
            activity?.replaceFragment(R.id.main_frame,
                    LoginFragment(), "Signup Fragment")
        }

        btn_signup.onClick { signupUser() }

    }

    /**
     * SignUp the user with the FirebaseAuth
     */
    private fun signupUser() {
        val name = name_signup.text.toString()
        val email = email_signup.text.toString()
        val password = password_signup.text.toString()

        if (validateField(name, email, password)) {
            showProgressBar()
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        hideProgressBar()
                        if (task.isSuccessful) {
                            insertUserIntoFirestore(name, email)
                            alert("You have successfully signed up. Let's get started. ") {
                                yesButton {
                                    activity?.finish()
                                    startActivity(intentFor<ContactsActivity>().clearTop())
                                }
                            }.show() // todo add already user exist
                        } else {
                            toast(task.exception?.message.toString())
                        }
                    }
        }
    }

    /**
     * Insert the basic fields to the firestore "user" collection
     */
    private fun insertUserIntoFirestore(name: String, email: String) {
        val uid = auth.currentUser?.uid.toString()
        val user = mutableMapOf<String, Any>(
                USER_ID to uid,
                USER_NAME to name,
                USER_EMAIL to email,
                USER_IMAGE_URL to "",
                IS_ONLINE to "")

        firestore.collection("users")
                .document(uid)
                .set(user)
                .addOnCompleteListener { task ->
                    when {
                        task.isSuccessful -> toast("User added to firestore")
                        else -> toast(task.exception?.message.toString())
                    }
                }
    }

    private fun showProgressBar() {
        progress_bar_signup.show()
        linear_layout_signup.setBackgroundColor(Color.parseColor("#e0e0e0"))
        activity?.window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun hideProgressBar() {
        progress_bar_signup.hide()
        linear_layout_signup.setBackgroundColor(Color.WHITE)
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun validateField(name: String, email: String, password: String): Boolean {
        return when {
            name.isEmpty() || email.isEmpty() || password.isEmpty() -> {
                toast("Some Field(s) are Empty")
                false
            }
            else -> true
        }
    }

}
