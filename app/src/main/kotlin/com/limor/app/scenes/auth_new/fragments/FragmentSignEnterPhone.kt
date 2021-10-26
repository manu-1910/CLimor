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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.limor.app.R
import com.limor.app.extensions.hideKeyboard
import com.limor.app.scenes.auth_new.AuthActivityNew
import com.limor.app.scenes.auth_new.AuthActivityNew.Companion.onFocusChangeListener
import com.limor.app.scenes.auth_new.AuthViewModelNew
import com.limor.app.scenes.auth_new.data.Country
import com.limor.app.scenes.auth_new.data.SignInMethod
import com.limor.app.scenes.auth_new.util.AfterTextWatcher
import kotlinx.android.synthetic.main.fragment_new_auth_phone_enter.*
import kotlinx.android.synthetic.main.fragment_new_auth_phone_enter.btnContinue
import kotlinx.android.synthetic.main.fragment_new_auth_phone_enter.clMain
import kotlinx.android.synthetic.main.fragment_new_auth_phone_enter.etEnterPhone
import kotlinx.android.synthetic.main.fragment_new_auth_phone_enter.etEnterPhoneInner
import kotlinx.android.synthetic.main.fragment_new_auth_phone_enter.etPhoneCode
import kotlinx.android.synthetic.main.fragment_new_auth_sign_in.*
import timber.log.Timber
import java.lang.ref.WeakReference


class FragmentSignEnterPhone : Fragment() {
    private val model: AuthViewModelNew by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        model.loadCountriesList(requireActivity().assets)
        return inflater.inflate(R.layout.fragment_new_auth_phone_enter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTextChangeListener()
        setClickListeners()
        setFocusChanges()
        subscribeToViewModel()
    }

    private fun setTextChangeListener() {
        etEnterPhoneInner.addTextChangedListener(object : AfterTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                model.setPhoneChanged(s?.toString() ?: "")
                etEnterPhone.error = null

                // the if avoid redraws
                if (tvSignInHereMsg.visibility == View.VISIBLE) {
                    tvSignInHereMsg.visibility = View.GONE
                }
            }
        })
    }

    private fun setClickListeners() {
        btnContinue.setOnClickListener {
            model.checkPhoneNumberExistence()
        }
        btnBack.setOnClickListener {
            AuthActivityNew.popBackStack(requireActivity())
        }

        tvSingUpTerms.setOnClickListener {
            (activity as AuthActivityNew).launchTermsUrl()
        }

        clMain.setOnClickListener {
            clMain.requestFocus()
        }

        vCountryCode.setOnClickListener {
            it.findNavController()
                .navigate(R.id.action_fragment_new_auth_sign_in_to_fragment_country_code)
        }
    }

    private fun setFocusChanges() {
        clMain.onFocusChangeListener = onFocusChangeListener()
        etPhoneCode.editText?.onFocusChangeListener = onFocusChangeListener()
    }

    private fun subscribeToViewModel() {
        model.initPhoneAuthHandler(WeakReference(requireActivity()))
        model.countriesLiveData.observe(viewLifecycleOwner, Observer {
            setCountry(it)
        })

        model.phoneIsValidLiveData.observe(viewLifecycleOwner, Observer {
            btnContinue.isEnabled = it
            if (it)
                clMain.hideKeyboard()
        })
        model.phoneNumberExistsLiveData.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            if (it == true) {
                val description = resources.getString(R.string.sign_in_with_phone_number)
                val content = SpannableString(description)
                content.setSpan(ForegroundColorSpan(resources.getColor(R.color.colorAccent)), 23, description.length, 0)
                content.setSpan(UnderlineSpan(), 23, description.length, 0)
                tvSignInHereMsg.setText(content, TextView.BufferType.SPANNABLE)
                tvSignInHereMsg.setOnClickListener {
                    model.setCurrentSignInMethod(SignInMethod.PHONE)
                    val args = Bundle()
                    args.putBoolean(FragmentSignIn.IS_MIGRATION_FLOW, false)
                    findNavController()
                        .navigate(R.id.action_fragment_new_auth_sign_in_to_fragment_sign_in, args)
                }
                etEnterPhone.setError(description)
                tvSignInHereMsg.visibility = View.VISIBLE
            } else {
                model.submitPhoneNumber()
                findNavController()
                    .navigate(R.id.action_fragment_new_auth_phone_enter_to_fragment_new_auth_phone_code)
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
            editText.setText(it.visualFormat, false)
        }
    }
}