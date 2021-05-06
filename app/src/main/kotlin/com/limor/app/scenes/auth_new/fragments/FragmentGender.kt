package com.limor.app.scenes.auth_new.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.limor.app.R
import com.limor.app.scenes.auth_new.AuthViewModelNew
import com.limor.app.scenes.auth_new.data.Gender
import kotlinx.android.synthetic.main.fragment_new_auth_gender.*

class FragmentGender : Fragment() {

    private val model: AuthViewModelNew by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_auth_gender, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListeners()
        setUpToggleButton()
    }

    private fun setOnClickListeners() {
        btnContinue.setOnClickListener {
//            it.findNavController()
//                .navigate(R.id.)
        }

        btnBack.setOnClickListener {
            it.findNavController().popBackStack()
        }

        btnSkip.setOnClickListener {
            model.setCurrentGender(Gender.None)
            //            it.findNavController()
//                .navigate(R.id.)
        }
    }

    private fun setUpToggleButton() {
        addToggleClickListener()
        setUpInitialGender()
    }

    private fun addToggleClickListener() {
        toggleGender.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            if (isChecked) {
                val gender = when (checkedId) {
                    R.id.btnGender1 -> Gender.Male
                    R.id.btnGender2 -> Gender.Female
                    R.id.btnGender3 -> Gender.Other
                    else -> Gender.Male
                }
                model.setCurrentGender(gender)
            }
        }
    }

    private fun setUpInitialGender() {
        val checkedId = when (model.currentGender) {
            Gender.Male -> R.id.btnGender1
            Gender.Female -> R.id.btnGender2
            Gender.Other -> R.id.btnGender3
            else -> R.id.btnGender1
        }
        toggleGender.check(checkedId)
    }
}