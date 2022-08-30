package com.limor.app.scenes.main_new.fragments

import android.app.Dialog
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager
import android.text.Editable
import android.text.Html
import android.view.*
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.limor.app.R
import com.limor.app.scenes.auth_new.AuthViewModelNew
import com.limor.app.scenes.auth_new.data.Country
import com.limor.app.scenes.auth_new.util.AfterTextWatcher
import com.limor.app.scenes.main.fragments.settings.EditProfileFragment
import com.limor.app.scenes.main.fragments.settings.SettingsViewModel
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_delete_account_phone_number_input.*
import kotlinx.android.synthetic.main.fragment_delete_account_phone_number_input.btnContinue
import kotlinx.android.synthetic.main.fragment_delete_account_phone_number_input.etEnterPhone
import kotlinx.android.synthetic.main.fragment_delete_account_phone_number_input.etEnterPhoneInner
import kotlinx.android.synthetic.main.fragment_new_auth_phone_enter.*
import kotlinx.android.synthetic.main.fragment_new_auth_phone_enter.etPhoneCode
import kotlinx.android.synthetic.main.fragment_new_auth_sign_in.*
import kotlinx.android.synthetic.main.fragment_verify_otp_for_account_deletion.*
import timber.log.Timber
import javax.inject.Inject

class FragmentDeleteAccountPhoneNumberInput : DialogFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: AuthViewModelNew by activityViewModels {viewModelFactory}
    private val settingsViewModel: SettingsViewModel by activityViewModels {viewModelFactory}

    private lateinit var currentUser: EditProfileFragment.UIUserUpdateModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_AppCompat_FloatingDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        model.loadCountriesList(requireActivity().assets)
        return inflater.inflate(
            R.layout.fragment_delete_account_phone_number_input,
            container,
            false
        )
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingsViewModel.showLogoInToolbar(false)
        setClickListeners()
        fetchRequiredData()
        setFocusListeners()
        setTextChangeListener()
        subscribeToViewModel()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    private fun setClickListeners(){
        countryView.setOnClickListener {
            findNavController().navigate(R.id.action_fragment_delete_account_to_fragment_country_code)
        }
        btnCancel.setOnClickListener {
            dismiss()
        }
        btnContinue.setOnClickListener {
            model.sendOtpToDeleteUserAccount()
            btnContinue.isEnabled = false
        }
    }

    private fun setFocusListeners(){
        etEnterPhoneInner.setOnFocusChangeListener { view, hasFocus ->
            if(hasFocus){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                    scrollView.setOnApplyWindowInsetsListener { _, windowInsets ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            val imeHeight = windowInsets.getInsets(WindowInsets.Type.ime()).bottom
                            scrollView.setPadding(0, 0, 0, imeHeight)
                            val insets = windowInsets.getInsets(WindowInsets.Type.ime() or WindowInsets.Type.systemGestures())
                            insets
                        }
                        windowInsets
                    }
                } else{
                    dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                }
            } else{
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                    scrollView.setOnApplyWindowInsetsListener { _, windowInsets ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            val imeHeight = windowInsets.getInsets(WindowInsets.Type.ime()).bottom
                            scrollView.setPadding(0, 0, 0, 0)
                            val insets = windowInsets.getInsets(WindowInsets.Type.ime() or WindowInsets.Type.systemGestures())
                            insets
                        }
                        windowInsets
                    }
                }
            }
        }
    }

    private fun fetchRequiredData(){
        settingsViewModel.getUserInfo()
        settingsViewModel.userInfoLiveData.observe(viewLifecycleOwner, Observer { user ->
            user?.let {
                currentUser = EditProfileFragment.UIUserUpdateModel.createFrom(it)
            }
            delete_limor_account_description.text = Html.fromHtml("Please verify your phone number associated with this <b><font color='black'>@" + currentUser.userName + "</font></b> Limor account", Html.FROM_HTML_MODE_COMPACT)
        })
    }

    private fun setTextChangeListener() {
        etEnterPhoneInner.addTextChangedListener(object : AfterTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                model.setPhoneChanged(s?.toString() ?: "")
                etEnterPhone.error = null
            }
        })
    }

    private fun subscribeToViewModel() {
        model.countriesLiveData.observe(viewLifecycleOwner, Observer {
            setCountry(it)
        })

        model.phoneIsValidLiveData.observe(viewLifecycleOwner, Observer {
            btnContinue.isEnabled = it
        })

        model.otpSentToDeleteUser.observe(viewLifecycleOwner, Observer {
            if(it == null)
                return@Observer
            if(it.toString().lowercase() == "success"){
                model.enableResend()
                Toast.makeText(activity, "Code has been sent", Toast.LENGTH_LONG)
                    .show()
                Handler(Looper.getMainLooper()).postDelayed(Runnable {
                    findNavController().navigate(R.id.action_fragment_delete_account_to_fragment_verify_otp)
                    dismiss()
                }, 2000)
            } else{
                btnContinue.isEnabled = false
                showErrorInSnackBar(it)
            }
        })

        model.countrySelectedManuallyLiveData.observe(viewLifecycleOwner, Observer{
            if(it == null){
                return@Observer
            }
            setCountry(model.countriesLiveData.value ?: emptyList())
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

    private fun showErrorInSnackBar(errorMessage: String){
        Snackbar.make(parentLayout, errorMessage, Snackbar.LENGTH_SHORT)
            .setTextColor(resources.getColor(android.R.color.white))
            .show()
    }

}
