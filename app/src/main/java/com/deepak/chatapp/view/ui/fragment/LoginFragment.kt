package com.deepak.chatapp.view.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.deepak.chatapp.R
import kotlinx.android.synthetic.main.fragment_signup.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class LoginFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        signup_btn.onClick { loginUser() }
    }

    private fun loginUser() {}

}
