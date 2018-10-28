package com.deepak.chatapp.view.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.deepak.chatapp.R
import com.deepak.chatapp.util.replaceFragment
import kotlinx.android.synthetic.main.fragment_main.*
import org.jetbrains.anko.sdk27.coroutines.onClick

/**
 * Show the App logo and GetStarted btn
 */
class MainFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        get_started_btn.onClick {
            activity?.replaceFragment(R.id.main_frame,
                    SignupFragment(), "Main Fragment")
        }

    }
}
