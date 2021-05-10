package com.limor.app.scenes.auth_new.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.limor.app.R
import com.limor.app.extensions.hideKeyboard
import com.limor.app.scenes.auth_new.AuthViewModelNew
import kotlinx.android.synthetic.main.fragment_new_auth_phone_code.*


class FragmentVerifyPhoneNumber : Fragment() {
    private val model: AuthViewModelNew by activityViewModels()
    private val smsCodeEtList = mutableListOf<TextInputLayout>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_auth_phone_code, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createSmsCodeFieldsList()
        setTextChangeListeners()
        setClickListeners()
        subscribeToViewModel()
    }

    private fun createSmsCodeFieldsList() {
        smsCodeEtList.add(etSms1)
        smsCodeEtList.add(etSms2)
        smsCodeEtList.add(etSms3)
        smsCodeEtList.add(etSms4)
        smsCodeEtList.add(etSms5)
        smsCodeEtList.add(etSms6)
    }

    private fun setTextChangeListeners() {
        for (et in smsCodeEtList) {
            val currentIndex = smsCodeEtList.indexOf(et)
            setOnTextChangedListener(et.editText!!, currentIndex)
            setOnDoneActionClicked(et.editText!!, currentIndex)
        }
    }

    private fun setOnTextChangedListener(et: EditText, currentIndex: Int) {
        et.doOnTextChanged { inputText, _, _, _ ->
            model.setSmsCodeForCheck(smsCodesList())

            if (inputText?.isNotEmpty() ?: false) {
                //user entered symbol
                if (currentIndex != smsCodeEtList.size - 1)
                    smsCodeEtList[currentIndex + 1].requestFocus()
            } else {
                if (currentIndex != 0)
                    smsCodeEtList[currentIndex - 1].requestFocus()
            }
        }
    }

    private fun smsCodesList(): List<String?> = smsCodeEtList.map { it.editText!!.text?.toString() }

    private fun setOnDoneActionClicked(et: EditText, currentIndex: Int) {
        et.setOnEditorActionListener { textView, actionId, keyEvent ->
            when (actionId and EditorInfo.IME_MASK_ACTION) {
                EditorInfo.IME_ACTION_DONE -> {
                    if (model.smsCodeIsFullLiveData.value == true)
                        validateSmsCode()
                }

                EditorInfo.IME_ACTION_NEXT -> {
                    if (currentIndex != smsCodeEtList.size - 1)
                        smsCodeEtList[currentIndex + 1].requestFocus()
                }
            }
            false
        }
    }

    private fun setClickListeners() {
        tvChangeNumber.setOnClickListener {
            it.findNavController().popBackStack()
        }

        btnContinue.setOnClickListener {
            it.hideKeyboard()
            validateSmsCode()
        }
    }

    private fun validateSmsCode() {
        model.submitSmsCode(smsCodesList())
    }

    private fun subscribeToViewModel() {
        tvPhone.text = model.formattedPhone
        model.smsCodeIsFullLiveData.observe(viewLifecycleOwner, Observer {
            btnContinue.isEnabled = it
        })

        model.smsCodeValidationErrorMessageLiveData.observe(viewLifecycleOwner, Observer {
            val hasError = it.isNotBlank()
            btnContinue.isEnabled = !hasError
            tvWrongCode.visibility = if (hasError) View.VISIBLE else View.GONE
            smsCodeEtList.forEach { et ->
                et.error = if (hasError) it else null
                et.editText!!.setTextColor(
                    resources
                        .getColor(if (hasError) R.color.error_stroke_color else R.color.black)
                )
            }
        })

        model.smsCodeValidatedLiveData.observe(viewLifecycleOwner, Observer {
            if (it)
                clMain.findNavController()
                    .navigate(R.id.action_fragment_new_auth_phone_code_to_fragment_new_auth_enter_email)
        })

    }
}