package com.limor.app.scenes.auth_new.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.limor.app.R
import com.limor.app.scenes.auth_new.AuthViewModelNew
import kotlinx.android.synthetic.main.fragment_new_auth_sign_in_check_email.*


class FragmentSignInCheckEmail : Fragment() {
    private val model: AuthViewModelNew by activityViewModels()
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
        subscribeToViewModel()
    }

    private fun subscribeToViewModel() {
        model.handleEmailDynamicLinkLiveData.observe(viewLifecycleOwner, Observer {
            if(it == true){
                btnFinish.findNavController()
                    .navigate(R.id.action_fragment_new_auth_sign_in_email_to_destination_main_activity)
                requireActivity().finish()
            }
        })
    }

    private fun setOnClicks() {
        btnFinish.setOnClickListener {
            val intent: Intent = requireActivity().packageManager
                .getLaunchIntentForPackage("com.google.android.gm") ?: return@setOnClickListener
            try{
                startActivity(intent)
            }catch (e:Exception){
                Toast.makeText(
                    requireContext(),
                    R.string.no_gmail_app_installed,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
