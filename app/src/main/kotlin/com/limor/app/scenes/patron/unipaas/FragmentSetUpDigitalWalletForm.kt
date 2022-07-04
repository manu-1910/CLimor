package com.limor.app.scenes.patron.unipaas

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.telephony.TelephonyManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.facebook.common.Common
import com.limor.app.BuildConfig
import com.limor.app.R
import com.limor.app.databinding.FragmentSetUpDigitalWalletFormBinding
import com.limor.app.di.Injectable
import com.limor.app.extensions.hideKeyboard
import com.limor.app.extensions.toCalendar
import com.limor.app.scenes.auth_new.AuthViewModelNew
import com.limor.app.scenes.auth_new.data.Country
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.uimodels.UserUIModel
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.fragment_first_and_last_name.*
import kotlinx.android.synthetic.main.fragment_new_auth_sign_in.*
import kotlinx.android.synthetic.main.fragment_new_auth_sign_in.etPhoneCode
import kotlinx.android.synthetic.main.fragment_set_up_digital_wallet_form.*
import kotlinx.android.synthetic.main.toolbar_with_2_icons.*
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.textColor
import org.jetbrains.anko.toast
import timber.log.Timber
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalDateTime.ofInstant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject


class FragmentSetUpDigitalWalletForm : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: AuthViewModelNew by activityViewModels { viewModelFactory }

    val user: UserUIModel? by lazy {
        activity?.intent?.extras?.getParcelable("user")
    }
    private lateinit var binding: FragmentSetUpDigitalWalletFormBinding

    private var myCalendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        model.loadCountriesList(requireActivity().assets)
        binding = FragmentSetUpDigitalWalletFormBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToViewModel()
        setClickListeners()
        setHelperTexts()
        setFocusListener()
        setDefaults()
        setListeners()
    }

    private fun setDefaults() {
        binding.etEnterDOBInner.setText(CommonsKt.getFormattedLocalDate(user?.dateOfBirth))
        myCalendar = user?.dateOfBirth?.toCalendar() ?: Calendar.getInstance()
        binding.etEnterFirstNameInner.setText(user?.firstName)
        binding.etEnterLastNameInner.setText(user?.lastName)
    }

    private fun subscribeToViewModel() {
        model.countriesLiveData.observe(viewLifecycleOwner, Observer {
            setCountry(it)
        })
        model.phoneIsValidLiveData.observe(viewLifecycleOwner, Observer {
            if (it) {
                mainLayout.hideKeyboard()
                enableSubmitButton()
            } else{
                binding.btnContinue.isEnabled = false
            }
        })
    }

    private fun setCountry(countries: List<Country>) {
        val editText = binding.etPhoneCode.editText as AutoCompleteTextView
        val tM = requireContext().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val countryCodeValue = tM.networkCountryIso
        // Hardcoded for now
        val country: Country? = countries.find { it.codeLetters.lowercase() == countryCodeValue}
        Timber.d("${country?.codeLetters}  $countryCodeValue")
        if(model.countrySelected == null){
            country?.let{
                model.setCountrySelected(country)
            }
        }
        model.countrySelected?.let {
            editText.setText(it.visualFormat)
            editText.isEnabled = false
        }
        enableSubmitButton()
    }

    private fun setClickListeners() {
        binding.btnContinue.setOnClickListener {
            validateInputAndContinue()
        }
        binding.vCountryCode.setOnClickListener {
            findNavController().navigate(R.id.action_set_up_digital_wallet_form_fragment_to_country_code_selection)
        }
    }

    private fun setListeners(){
        binding.etEnterFirstNameInner.addTextChangedListener(GenericTextWatcher(binding.etEnterFirstNameInner, ::enableSubmitButton))
        binding.etEnterLastNameInner.addTextChangedListener(GenericTextWatcher(binding.etEnterLastNameInner, ::enableSubmitButton))
        binding.etEnterEmailInner.addTextChangedListener(GenericTextWatcher(binding.etEnterEmailInner, ::enableSubmitButton))
        binding.etEnterPhoneInner.addTextChangedListener(GenericTextWatcher(binding.etEnterPhoneInner, ::validatePhoneNumber))
    }

    private fun setHelperTexts(){
        binding.etEnterFirstName.helperText = getString(R.string.required)
        binding.etEnterLastName.helperText = getString(R.string.required)
        binding.etEnterEmail.helperText = getString(R.string.required)
        binding.etEnterPhone.helperText = getString(R.string.required)
    }

    private fun setFocusListener(){
        binding.etEnterFirstNameInner.setOnFocusChangeListener { view, b ->
            if(!b){
                if(binding.etEnterFirstNameInner.text.toString().isEmpty()){
                    binding.etEnterFirstName.error = getString(R.string.cant_be_empty)
                } else{
                    binding.etEnterFirstName.error = null
                    binding.etEnterFirstName.helperText = getString(R.string.required)
                }
            } else{
                binding.etEnterFirstName.error = null
                binding.etEnterFirstName.helperText = getString(R.string.required)
            }
        }
        binding.etEnterLastNameInner.setOnFocusChangeListener { view, b ->
            if(!b){
                if(binding.etEnterLastNameInner.text.toString().isEmpty()){
                    binding.etEnterLastName.error = getString(R.string.cant_be_empty)
                } else{
                    binding.etEnterLastName.error = null
                    binding.etEnterLastName.helperText = getString(R.string.required)
                }
            } else{
                binding.etEnterLastName.error = null
                binding.etEnterLastName.helperText = getString(R.string.required)
            }
        }
        binding.etEnterPhoneInner.setOnFocusChangeListener { view, b ->
            if(!b){
                if(binding.etEnterPhoneInner.text.toString().isEmpty()){
                    binding.etEnterPhone.error = getString(R.string.cant_be_empty)
                } else{
                    binding.etEnterPhone.error = null
                    binding.etEnterPhone.helperText = getString(R.string.required)
                }
            } else{
                binding.etEnterPhone.error = null
                binding.etEnterPhone.helperText = getString(R.string.required)
            }
        }
        binding.etEnterEmailInner.setOnFocusChangeListener { view, b ->
            if(!b){
                if(binding.etEnterEmailInner.text.toString().isEmpty()){
                    binding.etEnterEmail.error = getString(R.string.cant_be_empty)
                } else{
                    binding.etEnterEmail.error = null
                    binding.etEnterEmail.helperText = getString(R.string.required)
                }
            } else{
                binding.etEnterEmail.error = null
                binding.etEnterEmail.helperText = getString(R.string.required)
            }
        }
    }

    private fun validatePhoneNumber(){
        model.setPhoneChanged(binding.etEnterPhoneInner.text.toString())
    }

    private fun validateInputAndContinue() {

        if(binding.etEnterFirstNameInner.text.isNullOrEmpty()){
            binding.etEnterFirstNameInner.error = "Required"
            binding.etEnterFirstNameInner.requestFocus()
            return
        }

        if(binding.etEnterLastNameInner.text.isNullOrEmpty()){
            binding.etEnterLastNameInner.error = "Required"
            binding.etEnterLastNameInner.requestFocus()
            return
        }

        if(!AuthViewModelNew.isEmailValid(binding.etEnterEmailInner.text.toString())){
            binding.etEnterEmailInner.error = "Enter a valid email"
            binding.etEnterEmailInner.requestFocus()
            return
        }

        if(binding.etEnterPhoneInner.text.isNullOrEmpty()){
            binding.etEnterPhoneInner.error = "Required"
            binding.etEnterPhoneInner.requestFocus()
            return
        } else if(model.phoneIsValidLiveData.value == false){
            binding.etEnterPhoneInner.error = "Invalid phone number"
            binding.etEnterPhoneInner.requestFocus()
            return
        }

        if(model.countrySelected?.code == null){
            binding.root.snackbar("Select a country code")
            return
        }
        val sdf: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val dateFormat = sdf.format(myCalendar.time)

        model.createVendor(
            binding.etEnterFirstNameInner.text.toString(),
            binding.etEnterLastNameInner.text.toString(),
            binding.etEnterEmailInner.text.toString(),
            "${model.countrySelected?.code}${binding.etEnterPhoneInner.text.toString()}",
            dateFormat
        ).observe(viewLifecycleOwner) { response ->
            if (BuildConfig.DEBUG) {
                Timber.d("WALLET -> $response")
            }
            if (!response.callWasSuccessful) {
                Toast.makeText(
                    binding.root.context,
                    "Error: ${response.errorMessage}",
                    Toast.LENGTH_LONG
                ).show()
                return@observe
            }
            response.result?.let { url ->
                PrefsHandler.setOnboardingUrl(requireContext(), url)

                // Take user to success screen
                findNavController().navigate(R.id.action_set_up_digital_wallet_form_fragment_to_set_up_digital_wallet_confirmation,
                bundleOf("url" to url))
            }

        }
    }

    private fun enableSubmitButton(){
        binding.btnContinue.isEnabled = (
                binding.etEnterFirstNameInner.text.toString().isNotEmpty() &&
                binding.etEnterLastNameInner.text.toString().isNotEmpty() &&
                binding.etEnterDOBInner.text.toString().isNotEmpty() &&
                binding.etEnterCountryInner.text.toString().isNotEmpty() &&
                binding.etEnterEmailInner.text.toString().isNotEmpty() &&
                binding.etEnterPhoneInner.text.toString().isNotEmpty() &&
                model.phoneIsValidLiveData.value == true &&
                model.countrySelected != null
        )
    }

}

class GenericTextWatcher internal constructor(
    private val view: View,
    private val afterTextChange: () -> Unit
) :
    TextWatcher {
    override fun afterTextChanged(editable: Editable) {}

    override fun beforeTextChanged(
        arg0: CharSequence,
        arg1: Int,
        arg2: Int,
        arg3: Int
    ) {}

    override fun onTextChanged(
        arg0: CharSequence,
        arg1: Int,
        arg2: Int,
        arg3: Int
    ) {
        afterTextChange()
    }
}