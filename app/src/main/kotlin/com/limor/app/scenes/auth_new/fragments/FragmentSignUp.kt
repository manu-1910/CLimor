package com.limor.app.scenes.auth_new.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.limor.app.R
import com.limor.app.scenes.auth_new.AuthActivityNew
import com.limor.app.scenes.auth_new.AuthViewModelNew
import kotlinx.android.synthetic.main.fragment_new_auth_sign_up.*

class FragmentSignUp : Fragment() {
    private val model: AuthViewModelNew by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_auth_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initNavigation()
        subscribeToModel()
    }

    private fun subscribeToModel() {
        model.googleSignIsComplete.observe(viewLifecycleOwner, Observer {
            if(it)
                clMain.findNavController()
                    .navigate(R.id.action_fragment_new_auth_sign_up_to_fragment_new_auth_dob_picker)
        })
    }

    private fun initNavigation() {
        btnSingUpNewPickPhone.setOnClickListener {
            it.findNavController()
                .navigate(R.id.action_fragment_new_auth_sign_up_to_fragment_new_auth_dob_picker)
        }

        btnSingUpNewGoogle.setOnClickListener {
            model.startGoogleAuth(requireActivity())
        }

        tvSignUpNewSignIn.setOnClickListener {
            it.findNavController()
                .navigate(R.id.action_fragment_new_auth_sign_up_to_fragment_new_auth_sign_in)
        }

        tvSingUpTerms.setOnClickListener{
            (activity as AuthActivityNew).launchTermsUrl()
        }
    }
}