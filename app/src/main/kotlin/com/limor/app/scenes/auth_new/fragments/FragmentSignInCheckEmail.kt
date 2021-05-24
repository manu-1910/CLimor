package com.limor.app.scenes.auth_new.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.limor.app.R
import kotlinx.android.synthetic.main.fragment_new_auth_onboarding.*

class FragmentSignInCheckEmail : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_auth_sign_in_check_email, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClicks()
    }

    private fun setOnClicks() {
        btnFinish.setOnClickListener {
            it.findNavController()
                .navigate(R.id.action_fragment_new_auth_sign_in_email_to_destination_main_activity)
            requireActivity().finish()
        }
    }
}
