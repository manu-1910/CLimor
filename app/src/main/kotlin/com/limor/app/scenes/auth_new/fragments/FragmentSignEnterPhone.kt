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
        btnBack.setOnClickListener {
            it.findNavController().popBackStack()
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

    private fun onFocusChangeListener(): View.OnFocusChangeListener {
        return View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus)
                v.hideKeyboard()
        }
    }

    private fun subscribeToViewModel() {
        model.setCountrySelected(Country())
        model.setPhoneChanged("")
        model.countriesLiveData.observe(viewLifecycleOwner, Observer {
            setCountriesAdapter(it)
        })

        model.phoneIsValidLiveData.observe(viewLifecycleOwner, Observer {
            btnContinue.isEnabled = it
            if(it)
                clMain.hideKeyboard()
        })
    }

    private fun setCountriesAdapter(countries: List<Country>) {
        val items = countries.map { it.visualFormat }
        val adapter = ArrayAdapter(requireContext(), R.layout.item_phone_code_country_code, items)
        (etPhoneCode.editText as? AutoCompleteTextView)?.setAdapter(adapter)
        (etPhoneCode.editText as? AutoCompleteTextView)?.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id -> model.setCountrySelected(countries[position]) }
    }
}