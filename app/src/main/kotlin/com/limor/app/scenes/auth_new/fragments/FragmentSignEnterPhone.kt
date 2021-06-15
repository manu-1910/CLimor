package com.limor.app.scenes.auth_new.fragments

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.limor.app.R
import com.limor.app.extensions.hideKeyboard
import com.limor.app.scenes.auth_new.AuthActivityNew
import com.limor.app.scenes.auth_new.AuthActivityNew.Companion.onFocusChangeListener
import com.limor.app.scenes.auth_new.AuthViewModelNew
import com.limor.app.scenes.auth_new.data.Country
import com.limor.app.scenes.auth_new.util.AfterTextWatcher
import kotlinx.android.synthetic.main.fragment_new_auth_phone_enter.*


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
            }
        })
    }

    private fun setClickListeners() {
        btnContinue.setOnClickListener {
            model.submitPhoneNumber()
            it.findNavController()
                .navigate(R.id.action_fragment_new_auth_phone_enter_to_fragment_new_auth_phone_code)
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
    }

    private fun setFocusChanges() {
        clMain.onFocusChangeListener = onFocusChangeListener()
        etPhoneCode.editText?.onFocusChangeListener = onFocusChangeListener()

    }

    private fun subscribeToViewModel() {
        model.initPhoneAuthHandler(requireActivity())
        model.countriesLiveData.observe(viewLifecycleOwner, Observer {
            setCountriesAdapter(it)
        })

        model.phoneIsValidLiveData.observe(viewLifecycleOwner, Observer {
            btnContinue.isEnabled = it
            if (it)
                clMain.hideKeyboard()
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
}