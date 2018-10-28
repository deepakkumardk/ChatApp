package com.deepak.chatapp.view.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.deepak.chatapp.R
import com.deepak.chatapp.util.hide
import com.deepak.chatapp.util.show
import com.deepak.chatapp.view.ui.ContactsActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_login.*
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.toast

/**
 * The login interface for the user
 */
class LoginFragment : Fragment() {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_login.onClick { loginUser() }
    }

    /**
     * Login the user with the FirebaseAuth
     */
    private fun loginUser() {
        val email = email_login.text.toString()
        val password = password_login.text.toString()

        if (validateField(email, password)) {
            showProgressBar()
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        hideProgressBar()
                        if (task.isSuccessful) {
                            activity?.finish()
                            startActivity(intentFor<ContactsActivity>().clearTop())
                        } else {
                            toast(task.exception?.message.toString())
                        }
                    }
        }
    }

    private fun showProgressBar() {
        progress_bar_login.show()
        linear_layout_login.setBackgroundColor(Color.GRAY)
        activity?.window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun hideProgressBar() {
        progress_bar_login.hide()
        linear_layout_login.setBackgroundColor(Color.WHITE)
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun validateField(email: String, password: String): Boolean {
        return when {
            email.isEmpty() || password.isEmpty() -> {
                toast("Email or Password is Empty")
                false
            }
            else -> true
        }
    }
}
