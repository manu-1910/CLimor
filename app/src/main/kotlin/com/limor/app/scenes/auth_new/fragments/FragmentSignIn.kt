package com.limor.app.scenes.auth_new.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager
import android.text.Editable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.limor.app.R
import com.limor.app.extensions.hideKeyboard
import com.limor.app.scenes.auth_new.AuthActivityNew
import com.limor.app.scenes.auth_new.AuthViewModelNew
import com.limor.app.scenes.auth_new.data.Country
import com.limor.app.scenes.auth_new.data.SignInMethod
import com.limor.app.scenes.auth_new.util.AfterTextWatcher
import com.limor.app.scenes.utils.LimorDialog
import kotlinx.android.synthetic.main.fragment_new_auth_phone_enter.*
import kotlinx.android.synthetic.main.fragment_new_auth_sign_in.*
import kotlinx.android.synthetic.main.fragment_new_auth_sign_in.btnContinue
import kotlinx.android.synthetic.main.fragment_new_auth_sign_in.clMain
import kotlinx.android.synthetic.main.fragment_new_auth_sign_in.etEnterPhone
import kotlinx.android.synthetic.main.fragment_new_auth_sign_in.etEnterPhoneInner
import kotlinx.android.synthetic.main.fragment_new_auth_sign_in.etPhoneCode
import kotlinx.android.synthetic.main.fragment_new_auth_sign_in.textView9
import kotlinx.android.synthetic.main.fragment_new_auth_sign_in_or_up.*
import kotlinx.android.synthetic.main.fragment_verify_otp_for_account_deletion.*
import timber.log.Timber
import java.lang.ref.WeakReference


class FragmentSignIn : Fragment() {

    companion object {
        const val IS_MIGRATION_FLOW = "IS_MIGRATION_FLOW"
    }

    private val model: AuthViewModelNew by activityViewModels()
    private val isMigrationFlow: Boolean by lazy { requireArguments().getBoolean(IS_MIGRATION_FLOW) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    performBack()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        model.loadCountriesList(requireActivity().assets)
        return inflater.inflate(R.layout.fragment_new_auth_sign_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFocusChanges()
        setClickListeners()
        setTextWatchers()
        setUpInitialSignUpState()
        setPhoneChangeListener()
        setEmailChangedListeners()
        subscribeToViewModel()
    }

    private fun setFocusChanges() {
        clMain.onFocusChangeListener = AuthActivityNew.onFocusChangeListener()
        etPhoneCode.editText?.onFocusChangeListener = AuthActivityNew.onFocusChangeListener()
    }

    private fun setTextWatchers(){
        etEnterPhoneInner.addTextChangedListener {  }
    }

    private fun setClickListeners() {
        val description = resources.getString(R.string.register)
        val content = SpannableString(description)
        content.setSpan(UnderlineSpan(), 0, description.length, 0)
        tvSignUpNewSignUp.setText(content, TextView.BufferType.SPANNABLE)
        tvExistingUserEmailSignInDesc.setOnClickListener {
            model.setCurrentSignInMethod(SignInMethod.EMAIL)
        }
        clMain.setOnClickListener {
            clMain.requestFocus()
        }
        tvSignUpNewSignUp.setOnClickListener {
            it.findNavController()
                .navigate(R.id.action_fragment_new_auth_sign_in_to_fragment_new_auth_dob_picker)
        }
        countryCodeTV.setOnClickListener {
            it.findNavController()
                .navigate(R.id.action_fragment_new_auth_sign_in_to_fragment_country_code)
        }
        backIV.setOnClickListener {
            performBack()
        }
        tvNoEmailExistErrorDesc.setOnClickListener {
            model.setCurrentSignInMethod(SignInMethod.NONE)
            it.findNavController()
                .navigate(R.id.action_fragment_new_auth_sign_in_to_fragment_new_auth_dob_picker)
        }
    }

    private fun performBack() {
        val isMigration =
            (model.signInMethodLiveData.value == SignInMethod.EMAIL) && isMigrationFlow
        if (isMigration) {
            this@FragmentSignIn.findNavController().navigateUp()
        } else {
            when (model.signInMethodLiveData.value) {
                SignInMethod.PHONE -> this@FragmentSignIn.findNavController().navigateUp()
                SignInMethod.EMAIL -> model.setCurrentSignInMethod(SignInMethod.PHONE)
            }
        }
    }

    private fun setUpInitialSignUpState() {
        if (isMigrationFlow) {
            model.setCurrentSignInMethod(SignInMethod.EMAIL)
        } else {
            model.setCurrentSignInMethod(SignInMethod.PHONE)
        }
    }

    private fun switchToSignInState(signInMethod: SignInMethod) {
        when (signInMethod) {
            SignInMethod.PHONE -> {
                etPhoneCode.visibility = View.VISIBLE
                etEnterPhone.visibility = View.VISIBLE
                etEnterEmail.visibility = View.GONE
                etEnterPhone.error = ""
                btnContinue.setText(R.string.get_otp)
                tvExistingUserEmailSignInDesc.visibility = View.GONE
                btnContinue.setOnClickListener {
                    btnContinue.isEnabled = false
                    model.checkPhoneNumberExistence()
                }
            }
            SignInMethod.EMAIL -> {
                etPhoneCode.visibility = View.GONE
                etEnterPhone.visibility = View.GONE
                etEnterEmail.visibility = View.VISIBLE
                tvExistingUserEmailSignInDesc.visibility = View.GONE
                textView9.setText(R.string.access_your_old_account)
                btnContinue.setText(R.string.send_login_link)
                btnContinue.setOnClickListener {
                    model.checkEmailIsInUse()
                }
            }
        }
    }

    /*Phone State*/

    private fun setPhoneChangeListener() {
        etEnterPhoneInner.addTextChangedListener(object : AfterTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                model.setPhoneChanged(s?.toString() ?: "")
                etEnterPhone.error = null
                tvExistingUserEmailSignInDesc.visibility = View.GONE
            }
        })
        etDobPickerInner.addTextChangedListener(object : AfterTextWatcher(){
            override fun afterTextChanged(s: Editable?) {
                etEnterEmail.error = null
                if(tvNoEmailExistErrorDesc.visibility == View.VISIBLE){
                    tvNoEmailExistErrorDesc.visibility = View.GONE
                }
            }
        })
    }

    private fun setCountry(countries: List<Country>) {
        val editText = etPhoneCode.editText as AutoCompleteTextView
        val tM = requireContext().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val countryCodeValue = tM.networkCountryIso
        val country: Country? = countries.find { it.codeLetters.lowercase() == countryCodeValue }
        Timber.d("${country?.codeLetters}  $countryCodeValue")
        if (model.countrySelected == null) {
            country?.let {
                model.setCountrySelected(country)
            }
        }
        model.countrySelected?.let {
            editText.setText(it.visualFormat)
        }
    }

    /*Email state*/

    private fun setEmailChangedListeners() {
        etEnterEmail.editText?.setText(model.currentEmail)
        etEnterEmail.editText?.doAfterTextChanged { model.changeCurrentEmail(it?.toString()) }
    }

    /*ViewModel*/

    private fun subscribeToViewModel() {
        model.initPhoneAuthHandler(WeakReference(requireActivity()))

        model.countriesLiveData.observe(viewLifecycleOwner, Observer {
            setCountry(it)
        })

        model.phoneIsValidLiveData.observe(viewLifecycleOwner, Observer {
            if (it)
                clMain.hideKeyboard()
        })

        model.signInMethodLiveData.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            switchToSignInState(it)
        })

        model.signInMethodContinueEnabledLiveData.observe(viewLifecycleOwner, Observer {
            btnContinue.isEnabled = it
        })

        model.emailLinkSentLiveData.observe(viewLifecycleOwner, Observer {
            if (it != true) return@Observer
            clMain.findNavController()
                .navigate(R.id.action_fragment_new_auth_sign_in_to_fragment_new_auth_sign_in_email)
        })

        model.emailAuthHandlerErrorLiveData.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            Toast.makeText(
                requireContext(),
                it,
                Toast.LENGTH_LONG
            ).show()
        })

        model.phoneNumberExistsLiveData.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            if(it.isFound == true && it.isDeleted == true){
                LimorDialog(layoutInflater).apply {
                    setTitle(R.string.reactivate_account)
                    setMessage(R.string.reactivate_message)
                    setIcon(R.drawable.ic_reactivate)
                    addButton(R.string.cancel, false)
                    addButton(R.string.continue_button, true) {
                        model.setReactivate(true)
                        model.sendOtp()
                    }
                }.show()
            }
            else if (it.isFound == true) {
                model.sendOtp()
            } else {
                btnContinue.isEnabled = true
                val description = resources.getString(R.string.sign_in_error_try_sign_up)
                tvExistingUserEmailSignInDesc.text = description
                tvExistingUserEmailSignInDesc.visibility = View.VISIBLE
                tvNoEmailExistErrorDesc.visibility = View.GONE
                etEnterPhone.error = description
            }
        })

        model.otpSent.observe(viewLifecycleOwner, Observer {
            if(it == null) return@Observer
            if(it.toString().lowercase() == "success"){
                Toast.makeText(activity, "Code has been sent", Toast.LENGTH_LONG)
                    .show()
                Handler(Looper.getMainLooper()).postDelayed(Runnable {
                    findNavController().navigate(R.id.action_fragment_new_auth_sign_in_to_fragment_new_auth_phone_code)
                }, 2000)
            } else{
                btnContinue.isEnabled = false
                showErrorInSnackBar(it)
            }
        })
    }

    private fun showErrorInSnackBar(errorMessage: String){
        Snackbar.make(clMain, if(errorMessage.trim() == "") "Something went wrong! Please try after sometime." else errorMessage, Snackbar.LENGTH_SHORT)
            .setTextColor(resources.getColor(android.R.color.white))
            .show()
    }

}