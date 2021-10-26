package com.limor.app.scenes.auth_new.fragments

import android.content.Context
import android.os.Bundle
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
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.limor.app.R
import com.limor.app.extensions.hideKeyboard
import com.limor.app.scenes.auth_new.AuthActivityNew
import com.limor.app.scenes.auth_new.AuthViewModelNew
import com.limor.app.scenes.auth_new.data.Country
import com.limor.app.scenes.auth_new.data.SignInMethod
import com.limor.app.scenes.auth_new.util.AfterTextWatcher
import kotlinx.android.synthetic.main.fragment_new_auth_phone_enter.*
import kotlinx.android.synthetic.main.fragment_new_auth_sign_in.*
import kotlinx.android.synthetic.main.fragment_new_auth_sign_in.btnContinue
import kotlinx.android.synthetic.main.fragment_new_auth_sign_in.clMain
import kotlinx.android.synthetic.main.fragment_new_auth_sign_in.etEnterPhone
import kotlinx.android.synthetic.main.fragment_new_auth_sign_in.etEnterPhoneInner
import kotlinx.android.synthetic.main.fragment_new_auth_sign_in.etPhoneCode
import kotlinx.android.synthetic.main.fragment_new_auth_sign_in.textView9
import kotlinx.android.synthetic.main.fragment_new_auth_sign_in_or_up.*
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
        setUpInitialSignUpState()
        setPhoneChangeListener()
        setEmailChangedListeners()
        subscribeToViewModel()
    }

    private fun setFocusChanges() {
        clMain.onFocusChangeListener = AuthActivityNew.onFocusChangeListener()
        etPhoneCode.editText?.onFocusChangeListener = AuthActivityNew.onFocusChangeListener()
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
            if (it) {
                model.submitPhoneNumber()
                findNavController()
                    .navigate(R.id.action_fragment_new_auth_sign_in_to_fragment_new_auth_phone_code)
            } else {
                val description = resources.getString(R.string.email_sign_in_message)
                val content = SpannableString(description)
                content.setSpan(
                    ForegroundColorSpan(resources.getColor(R.color.colorAccent)),
                    55,
                    60,
                    0
                )
                content.setSpan(UnderlineSpan(), 55, 60, 0)
                tvExistingUserEmailSignInDesc.setText(content, TextView.BufferType.SPANNABLE)
                tvExistingUserEmailSignInDesc.visibility = View.VISIBLE
                tvNoEmailExistErrorDesc.visibility = View.GONE
                etEnterPhone.setError(description)
            }
        })

        model.currentEmailIsInUseLiveData.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            if (it) {
//                everything is ok, user email exists on firebase DB
                model.sendFirebaseDynamicLinkToEmail(requireContext())
                return@Observer
            }
            Timber.d("currentEmailIsInUseLiveData -> $it")
            val description = resources.getString(R.string.no_account_fount_with_email)
            val content = SpannableString(description)
            content.setSpan(
                ForegroundColorSpan(resources.getColor(R.color.colorAccent)),
                40,
                description.length,
                0
            )
            content.setSpan(UnderlineSpan(), 40, description.length, 0)
            tvNoEmailExistErrorDesc.setText(content, TextView.BufferType.SPANNABLE)
            tvNoEmailExistErrorDesc.visibility = View.VISIBLE
            tvExistingUserEmailSignInDesc.visibility = View.GONE
            etEnterEmail.error = description
        })
    }
}