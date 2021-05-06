package com.limor.app.scenes.auth_new.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.limor.app.R
import kotlinx.android.synthetic.main.fragment_new_auth_sign_in_or_up.*
import timber.log.Timber

class FragmentSignInOrUp : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_auth_sign_in_or_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnSignInNew.setOnClickListener {
            Timber.d("SignIn Clicked")
            view.findNavController()
                .navigate(R.id.action_fragment_new_auth_sign_in_or_up_to_fragment_new_auth_sign_in)
        }

        btnSignUpNew.setOnClickListener {
            Timber.d("SignUp Clicked")
            val destinationId =
//                if (BuildConfig.DEBUG)
//                    R.id.action_fragment_new_auth_sign_in_or_up_to_fragment_new_auth_gender
//                else
                    R.id.action_fragment_new_auth_sign_in_or_up_to_fragment_new_auth_sign_up
            view.findNavController()
                .navigate(destinationId)
        }
    }
}