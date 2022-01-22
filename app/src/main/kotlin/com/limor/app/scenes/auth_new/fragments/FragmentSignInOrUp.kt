package com.limor.app.scenes.auth_new.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.firebase.FirebaseApp
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.extensions.throttledClick
import kotlinx.android.synthetic.main.fragment_new_auth_sign_in.*
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

    private fun navigateToSignIn() {
        val navigateTo = R.id.action_fragment_new_auth_sign_in_or_up_to_fragment_new_auth_sign_in
        Bundle().apply {
            putBoolean(FragmentSignIn.IS_MIGRATION_FLOW, false)
        }.also { args ->
            requireView().findNavController().navigate(navigateTo, args)
        }
    }

    private fun navigateToSignUp() {
        val navigateTo = R.id.action_fragment_new_auth_sign_in_or_up_to_fragment_new_auth_sign_up
        requireView().findNavController().navigate(navigateTo)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnSignInNew.throttledClick {
            if (BuildConfig.DEBUG) {
                Timber.d("Sign IN Clicked")
            }
            navigateToSignIn()
        }

        btnSignUpNew.throttledClick {
            if (BuildConfig.DEBUG) {
                Timber.d("Sign UP Clicked")
            }
            navigateToSignUp()
        }

        tosHint.movementMethod = LinkMovementMethod.getInstance()

        //addVersionInfo()
    }

    @SuppressLint("SetTextI18n")
    private fun addVersionInfo() {
        // turns our this is only allowed on development (should not show up on staging and beta)
        // for sure not on production
        if (BuildConfig.AWS_S3_BUCKET.indexOf("-development") > 0) {
            val fbInfo = "Firebase project ID: ${FirebaseApp.getInstance().options.projectId}"
            val info =
                "${BuildConfig.VERSION_CODE} (${BuildConfig.VERSION_NAME}) on development backend"
            textView7.text = "${textView7.text}\n\n$info\n\n$fbInfo"
        }
    }

}