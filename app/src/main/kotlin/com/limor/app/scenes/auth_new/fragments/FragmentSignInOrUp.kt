package com.limor.app.scenes.auth_new.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.SpannableString
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnSignInNew.setOnClickListener {
            val args = Bundle()
            args.putBoolean(FragmentSignIn.IS_MIGRATION_FLOW, false)
            Timber.d("SignIn Clicked")
            view.findNavController()
                .navigate(
                    R.id.action_fragment_new_auth_sign_in_or_up_to_fragment_new_auth_sign_in,
                    args
                )
        }

        btnSignUpNew.setOnClickListener {
            Timber.d("SignUp Clicked")
            val destinationId =
//                if (BuildConfig.DEBUG)
//                    R.id.debugAction
//                else
                R.id.action_fragment_new_auth_sign_in_or_up_to_fragment_new_auth_sign_up
            view.findNavController()
                .navigate(destinationId)
        }
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