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
import com.limor.app.scenes.auth_new.AuthViewModelNew
import kotlinx.android.synthetic.main.fragment_new_auth_dob_picker.*


class FragmentDobPicker : Fragment() {

    private val model: AuthViewModelNew by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_auth_dob_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnDobPickerContinue.setOnClickListener {}

        btnDobPickerBack.setOnClickListener {
            it.findNavController().popBackStack()
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
        model.datePickedLiveData.observe(viewLifecycleOwner, Observer<String> {
            etDobPickerInner.setText(it ?: "")
            btnDobPickerContinue.isEnabled = it != null && it.isNotEmpty();
        })
    }
}