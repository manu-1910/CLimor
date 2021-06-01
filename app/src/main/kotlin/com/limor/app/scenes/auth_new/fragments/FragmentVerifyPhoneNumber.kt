package com.limor.app.scenes.auth_new.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.google.android.material.textfield.TextInputLayout
import com.limor.app.R
import com.limor.app.extensions.hideKeyboard
import com.limor.app.scenes.auth_new.AuthActivityNew
import com.limor.app.scenes.auth_new.AuthViewModelNew
import com.limor.app.scenes.auth_new.navigation.AuthNavigator.navigateToFragmentByNavigationBreakpoints
import com.limor.app.scenes.auth_new.navigation.NavigationBreakpoints
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
                    if (model.smsContinueButtonEnabled.value == true)
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
            AuthActivityNew.popBackStack(requireActivity())
        }

        btnContinue.setOnClickListener {
            it.hideKeyboard()
            validateSmsCode()
        }

        fabResendCode.setOnClickListener {
            it.hideKeyboard()
            model.resendCode()
        }
    }

    private fun validateSmsCode() {
        model.submitSmsCode(smsCodesList())
    }

    @SuppressLint("SetTextI18n")
    private fun subscribeToViewModel() {
        tvPhone.text = model.formattedPhone
        model.smsContinueButtonEnabled.observe(viewLifecycleOwner, Observer {
            btnContinue.isEnabled = it
        })

        model.smsCodeValidationErrorMessage.observe(viewLifecycleOwner, Observer {
            val hasError = it.isNotBlank()
            tvWrongCode.visibility = if (hasError) View.VISIBLE else View.GONE
            smsCodeEtList.forEach { et ->
                et.error = if (hasError) " " else null
                et.editText!!.setTextColor(
                    resources
                        .getColor(if (hasError) R.color.error_stroke_color else R.color.black)
                )
            }
        })

        model.userInfoProviderErrorLiveData.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        })

        model.createUserLiveData.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            navigateToFragmentByNavigationBreakpoints(
                requireActivity(),
                NavigationBreakpoints.ACCOUNT_CREATION.destination
            )
        })

        model.breakPointLiveData.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            if (it == NavigationBreakpoints.ACCOUNT_CREATION.destination)
                model.createUser()
            else
                navigateToFragmentByNavigationBreakpoints(requireActivity(), it)

        })

        model.smsCodeValidationPassed.observe(viewLifecycleOwner, Observer {
            if (it != true) return@Observer
            model.getUserOnboardingStatus()
        })

        model.resendButtonEnableLiveData.observe(viewLifecycleOwner, Observer {
            fabResendCode.isEnabled = it
        })

        model.resendButtonCountDownLiveData.observe(viewLifecycleOwner, Observer {
            if (it == null)
                tvResendCodeStatus.setText(R.string.didnt_receive_sms_code)
            else
                tvResendCodeStatus.text =
                    getString(R.string.resend_code_in) + (if (it < 10) "0$it" else it)
        })
    }
}