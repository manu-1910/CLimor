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
import kotlinx.android.synthetic.main.fragment_new_auth_sign_in.*
import timber.log.Timber
import java.lang.ref.WeakReference


class FragmentSignIn : Fragment() {

    companion object{
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

        clMain.setOnClickListener {
            clMain.requestFocus()
        }
        tvSignUpNewSignUp.setOnClickListener {
            it.findNavController()
                .navigate(R.id.action_fragment_new_auth_sign_in_to_fragment_new_auth_dob_picker)
        }
        tvExistingUserEmailSignInDesc.setOnClickListener {
            model.setCurrentSignInMethod(SignInMethod.EMAIL)
        }
        countryCodeTV.setOnClickListener {
            it.findNavController()
                .navigate(R.id.action_fragment_new_auth_sign_in_to_fragment_country_code)
        }
        backIV.setOnClickListener {
            performBack()
        }
    }

    private fun performBack(){
        val isMigration = (model.signInMethodLiveData.value == SignInMethod.EMAIL) && isMigrationFlow
        if(isMigration){
            this@FragmentSignIn.findNavController().navigateUp()
        } else{
            when (model.signInMethodLiveData.value) {
                SignInMethod.PHONE -> this@FragmentSignIn.findNavController().navigateUp()
                SignInMethod.EMAIL -> model.setCurrentSignInMethod(SignInMethod.PHONE)
            }
        }
    }

    private fun setUpInitialSignUpState() {
        if(isMigrationFlow){
            model.setCurrentSignInMethod(SignInMethod.EMAIL)
        } else{
            model.setCurrentSignInMethod(SignInMethod.PHONE)
        }
    }

    private fun switchToSignInState(signInMethod: SignInMethod) {
        when (signInMethod) {
            SignInMethod.PHONE -> {
                etPhoneCode.visibility = View.VISIBLE
                etEnterPhone.visibility = View.VISIBLE
                etEnterEmail.visibility = View.GONE
                //tvExistingUserEmailSignInDesc.visibility = View.VISIBLE
                //tvSignInHere.visibility = View.VISIBLE
                btnContinue.setText(R.string.get_otp)
                textView9.setText(R.string.sign_in)
                val description = resources.getString(R.string.existing_user_email_signin_description) + resources.getString(R.string.sign_in_here)
                val content = SpannableString(description)
                content.setSpan(ForegroundColorSpan(resources.getColor(R.color.colorAccent)), description.lastIndexOf('S'), description.length, 0)
                content.setSpan(UnderlineSpan(), description.lastIndexOf('S'), description.length, 0)
                tvExistingUserEmailSignInDesc.setText(content, TextView.BufferType.SPANNABLE)
                btnContinue.setOnClickListener {
                    model.submitPhoneNumber()
                    it.findNavController()
                        .navigate(R.id.action_fragment_new_auth_sign_in_to_fragment_new_auth_phone_code)
                }
            }
            SignInMethod.EMAIL -> {
                etPhoneCode.visibility = View.GONE
                etEnterPhone.visibility = View.GONE
                etEnterEmail.visibility = View.VISIBLE
                //tvExistingUserEmailSignInDesc.visibility = View.GONE
                //tvSignInHere.visibility = View.GONE
                tvExistingUserEmailSignInDesc.setText(R.string.email_login_only_for_users)
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
            }
        })
    }

    private fun setCountry(countries: List<Country>) {
        val editText = etPhoneCode.editText as AutoCompleteTextView
        val tM = requireContext().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val countryCodeValue = tM.networkCountryIso
        val country: Country? = countries.find { it.codeLetters.lowercase() == countryCodeValue}
        Timber.d("${country?.codeLetters}  $countryCodeValue")
        if(model.countrySelected == null){
            country?.let{
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

        model.currentEmailIsInUseLiveData.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            if (it) {
//                everything is ok, user email exists on firebase DB
                model.sendFirebaseDynamicLinkToEmail(requireContext())
                return@Observer
            }
            Timber.d("currentEmailIsInUseLiveData -> $it")
            Toast.makeText(
                requireContext(),
                R.string.no_email_found_offer_to_sign_up,
                Toast.LENGTH_LONG
            ).show()
        })
    }
}