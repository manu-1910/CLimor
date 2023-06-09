package com.limor.app.scenes.auth_new.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.limor.app.R
import com.limor.app.extensions.hideKeyboard
import com.limor.app.scenes.auth_new.AuthActivityNew
import com.limor.app.scenes.auth_new.AuthViewModelNew
import com.limor.app.scenes.auth_new.navigation.AuthNavigator.navigateToFragmentByNavigationBreakpoints
import com.limor.app.scenes.auth_new.navigation.NavigationBreakpoints
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.auth_new.util.ToastMaker
import kotlinx.android.synthetic.main.fragment_new_auth_phone_code.*
import kotlinx.android.synthetic.main.fragment_new_auth_phone_code.btnContinue
import kotlinx.android.synthetic.main.fragment_new_auth_phone_code.etSms1
import kotlinx.android.synthetic.main.fragment_new_auth_phone_code.etSms2
import kotlinx.android.synthetic.main.fragment_new_auth_phone_code.etSms3
import kotlinx.android.synthetic.main.fragment_new_auth_phone_code.etSms4
import kotlinx.android.synthetic.main.fragment_new_auth_phone_code.etSms5
import kotlinx.android.synthetic.main.fragment_new_auth_phone_code.etSms6
import kotlinx.android.synthetic.main.fragment_verify_otp_for_account_deletion.*


class FragmentVerifyPhoneNumber : Fragment() {
    private val model: AuthViewModelNew by activityViewModels()
    private val smsCodeEtList = mutableListOf<TextInputLayout>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        model.enableResend()
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
        et.doAfterTextChanged {
            resetErrorMessages()
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
            validateSmsCode()
        }

        fabResendCode.setOnClickListener {
            it.hideKeyboard()
            fabResendCode.isEnabled = false
            model.resendCode()
        }
    }

    private fun validateSmsCode() {
        btnContinue.isEnabled = false
        btnContinue.hideKeyboard()
        resetErrorMessages()
        model.submitSmsCode(smsCodesList())
    }

    private fun saveUserDOBAndProceed() {
        model.updateDOB()
    }

    private fun resetErrorMessages() {
        tvWrongCode.visibility = View.GONE
        tvWrongCode.text = ""
        smsCodeEtList.forEach { et ->
            et.error = null
            et.editText?.setTextColor(ContextCompat.getColor(et.context, R.color.black))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun subscribeToViewModel() {
        tvPhone.text = model.formattedPhone
        model.clearSmsCodeError()
        model.smsContinueButtonEnabled.observe(viewLifecycleOwner, Observer {
            btnContinue.isEnabled = it
        })

        model.userInfoProviderErrorLiveData.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            ToastMaker.showToast(requireContext(), it)
        })

        model.createUserLiveData.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            model.saveNavigationBreakPoint(
                requireContext(),
                NavigationBreakpoints.NAME_COLLECTION.destination
            )
        })

        model.navigationBreakPointLiveData.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer

            // When a migrating user has entered their DOB and verified their phone number but
            // hasn't changed their onboarding status we need to update the backend with their
            // newly entered DOB and change the onboarding status
            if (it == NavigationBreakpoints.DOB_PHONE_COLLECTION.destination) {
                saveUserDOBAndProceed()
            } else {
                navigateToBreakPoint(it)
            }
        })

        model.smsCodeValidationPassed.observe(viewLifecycleOwner, Observer {
            if (it != true) return@Observer
            model.checkJwtForLuidAndProceed()
        })

        model.resendButtonEnableLiveData.observe(viewLifecycleOwner, Observer {
            fabResendCode.isEnabled = it
        })
        model.resendButtonCountDownLiveData.observe(viewLifecycleOwner, Observer {
            if (it == null)
                tvResendCodeStatus.setText(R.string.didnt_receive_sms_code)
            else
                tvResendCodeStatus.text =
                    getString(R.string.resend_code_in) + (if (it < 10) " 0$it seconds" else " $it seconds")
        })

        model.updateUserDOBLiveData.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) {
                return@observe
            }
            when (it) {
                "Success" -> navigateToBreakPoint(NavigationBreakpoints.PREFERENCE_COLLECTION.destination)
                "Failure" -> showError(R.string.could_not_update_dob)
            }
        }

        model.otpSent.observe(viewLifecycleOwner, Observer {
            if(it == null)
                return@Observer
            if(it.toString().lowercase() == "success"){
                Toast.makeText(activity, "Code has been sent", Toast.LENGTH_LONG)
                    .show()
                model.enableResend()
            } else{
                showErrorInSnackBar(it)
            }
        })

        model.otpValid.observe(viewLifecycleOwner, Observer {
            if(it == null) return@Observer
            if(it.isNotEmpty()) {
               model.signInWithToken(it)
            } else{
                tvWrongCode.visibility = View.VISIBLE
                tvWrongCode.text = "Invalid code"
                fabResendCode.isEnabled = true
                model.cancelTimers()
                smsCodeEtList.forEach { et ->
                    et.error = " "
                    et.editText!!.setTextColor(
                        resources
                            .getColor(R.color.error_stroke_color)
                    )
                }

                // re-enable the button as the user might want to enter a new OTP
                btnContinue.isEnabled = true
            }
        })

        model.otpInValid.observe(viewLifecycleOwner, Observer {
            if(it == null) return@Observer
            fabResendCode.isEnabled = true
            model.cancelTimers()
            smsCodeEtList.forEach { et ->
                et.error = " "
                et.editText!!.setTextColor(
                    resources
                        .getColor(R.color.error_stroke_color)
                )
            }

            // re-enable the button as the user might want to enter a new OTP
            btnContinue.isEnabled = false
            showErrorInSnackBar(it)
        })

    }

    private fun showErrorInSnackBar(errorMessage: String){
        Snackbar.make(clMain, if(errorMessage.trim() == "") "Something went wrong! Please try after sometime." else errorMessage, Snackbar.LENGTH_SHORT)
            .setTextColor(resources.getColor(android.R.color.white))
            .show()
    }

    private fun showError(messageResId: Int) {
        Toast.makeText(requireContext(), messageResId, Toast.LENGTH_LONG).show()
    }

    private fun navigateToBreakPoint(breakPointDestination: String) {
        model.saveNavigationBreakPoint(requireContext(), breakPointDestination)
        if(breakPointDestination == NavigationBreakpoints.HOME_FEED.destination) {
            PrefsHandler.saveJustLoggedIn(requireContext(), true)
        }
        navigateToFragmentByNavigationBreakpoints(requireActivity(), breakPointDestination)
    }

    override fun onDestroy() {
        super.onDestroy()
        model.cancelTimers()
    }
}