package com.limor.app.scenes.auth_new.fragments

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.limor.app.R
import com.limor.app.extensions.hideKeyboard
import com.limor.app.scenes.auth_new.AuthActivityNew
import com.limor.app.scenes.auth_new.AuthViewModelNew
import com.limor.app.scenes.auth_new.data.Country
import com.limor.app.scenes.auth_new.data.SignInMethod
import com.limor.app.scenes.auth_new.util.AfterTextWatcher
import kotlinx.android.synthetic.main.fragment_new_auth_sign_in.*
import timber.log.Timber

class FragmentSignIn : Fragment() {

    private val model: AuthViewModelNew by activityViewModels()

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
        addToggleClickListener()
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
        clMain.setOnClickListener {
            clMain.requestFocus()
        }
        tvSignUpNewSignUp.setOnClickListener {
            it.findNavController()
                .navigate(R.id.action_fragment_new_auth_sign_in_to_fragment_new_auth_dob_picker)
        }
    }

    /*Toggle state*/

    private fun addToggleClickListener() {
        toggleSignInMethod.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            if (isChecked) {
                val signInMethod = when (checkedId) {
                    R.id.btnTogglePhone -> SignInMethod.PHONE
                    R.id.btnToggleEmail -> SignInMethod.EMAIL
                    else -> SignInMethod.PHONE
                }
                model.setCurrentSignInMethod(signInMethod)
                toggleButton.hideKeyboard()
            }
        }
    }

    private fun setUpInitialSignUpState() {
        if (model.signInMethodLiveData.value == null) {
            model.setCurrentSignInMethod(SignInMethod.PHONE)
        }

        val checkedId = when (model.signInMethodLiveData.value) {
            SignInMethod.PHONE -> R.id.btnTogglePhone
            SignInMethod.EMAIL -> R.id.btnToggleEmail
            else -> R.id.btnTogglePhone
        }
        toggleSignInMethod.check(checkedId)
    }

    private fun switchToSignInState(signInMethod: SignInMethod) {
        when (signInMethod) {
            SignInMethod.PHONE -> {
                etPhoneCode.visibility = View.VISIBLE
                etEnterPhone.visibility = View.VISIBLE
                etEnterEmail.visibility = View.GONE
                btnContinue.setText(R.string.get_otp)
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

    private fun setCountriesAdapter(countries: List<Country>) {
        val items = countries.map { it.visualFormat }
        val adapter = ArrayAdapter(requireContext(), R.layout.item_phone_code_country_code, items)
        val editText = etPhoneCode.editText as AutoCompleteTextView
        editText.setAdapter(adapter)
        editText.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                model.setCountrySelected(
                    countries[position]
                )
            }
        model.countrySelected?.let {
            editText.setText(it.visualFormat, false)
        }
    }

    /*Email state*/

    private fun setEmailChangedListeners() {
        etEnterEmail.editText?.setText(model.currentEmail)
        etEnterEmail.editText?.doAfterTextChanged { model.changeCurrentEmail(it?.toString()) }
    }

    /*ViewModel*/

    private fun subscribeToViewModel() {
        model.initPhoneAuthHandler(requireActivity())

        model.countriesLiveData.observe(viewLifecycleOwner, Observer {
            setCountriesAdapter(it)
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