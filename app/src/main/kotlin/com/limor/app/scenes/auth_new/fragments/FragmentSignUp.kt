package com.limor.app.scenes.auth_new.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.limor.app.R
import kotlinx.android.synthetic.main.fragment_new_auth_sign_up.*
import timber.log.Timber

class FragmentSignUp : Fragment() {
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
    }

    private fun initNavigation() {
        btnSingUpNewPickPhone.setOnClickListener {
            it.findNavController()
                .navigate(R.id.action_fragment_new_auth_sign_up_to_fragment_new_auth_dob_picker)
        }

        tvSignUpNewSignIn.setOnClickListener {
            it.findNavController()
                .navigate(R.id.action_fragment_new_auth_sign_up_to_fragment_new_auth_sign_in)
        }

        tvSingUpTerms.setOnClickListener{
            val uri = Uri.parse(TERMS_URL)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            try {
                startActivity(intent)
            } catch (e: Exception){
                Timber.e(e)
            }
        }
    }

    companion object{
        private const val TERMS_URL = "https://www.limor.ie/terms-and-conditions-of-use";
    }
}