package com.limor.app.scenes.auth_new.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.limor.app.R
import com.limor.app.scenes.auth_new.AuthActivityNew
import com.limor.app.scenes.auth_new.AuthViewModelNew
import com.limor.app.scenes.auth_new.data.DobInfo
import com.limor.app.scenes.auth_new.data.SignInMethod
import kotlinx.android.synthetic.main.fragment_new_auth_dob_picker.*


class FragmentDobPicker : Fragment() {

    private val model: AuthViewModelNew by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        model.setCurrentSignInMethod(SignInMethod.NONE)
        return inflater.inflate(R.layout.fragment_new_auth_dob_picker, container, false)
    }


    private fun navigateToPhoneAuth() {
        findNavController()
            .navigate(R.id.action_fragment_new_auth_dob_picker_to_fragment_new_auth_phone_enter)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnDobPickerContinue.setOnClickListener {
            navigateToPhoneAuth()
        }

        btnDobPickerBack.setOnClickListener {
            AuthActivityNew.popBackStack(requireActivity())
        }

        etDobPicker.setStartIconOnClickListener {
            model.startDobPicker(activity?.supportFragmentManager!!)
        }

        etDobPicker.setEndIconOnClickListener {
            model.clearDate()
        }

        etDobPicker.requestFocus()
        subscribeToViewModel()
    }

    private fun subscribeToViewModel() {
        model.datePickedLiveData.observe(viewLifecycleOwner, Observer<DobInfo> {
            etDobPickerInner.setText(it.formatted)
            btnDobPickerContinue.isEnabled = it.isValid()
            when {
                it.mills == 0L -> etDobPicker.error = null
                it.isValid() -> etDobPicker.error = null
                else -> etDobPicker.error = getString(R.string.date_less_than_13_yo)
            }
        })
    }
}