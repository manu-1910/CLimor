package com.limor.app.scenes.patron.unipaas

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
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
import com.limor.app.scenes.auth_new.AuthViewModelNew
import com.limor.app.scenes.auth_new.data.Country
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.uimodels.UserUIModel
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.fragment_new_auth_sign_in.*
import kotlinx.android.synthetic.main.toolbar_with_2_icons.*
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.toast
import timber.log.Timber
import java.text.SimpleDateFormat
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

    val myCalendar = Calendar.getInstance()

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
        binding = FragmentSetUpDigitalWalletFormBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToViewModel()
        setClickListeners()
        setDefaults()
    }

    private fun setDefaults() {
        binding.etEnterDOBInner.setText(CommonsKt.getFormattedLocalDate(user?.dateOfBirth))
    }

    private fun subscribeToViewModel() {
        model.countriesLiveData.observe(viewLifecycleOwner, Observer {
            setCountry(it)
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

    var date =
        OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabel()
        }

    private fun setClickListeners() {
        binding.btnContinue.setOnClickListener {
            validateInputAndContinue()
           // findNavController().navigate(R.id.action_set_up_digital_wallet_form_fragment_to_set_up_digital_wallet_confirmation)
        }

        binding.vCountryCode.setOnClickListener {
            findNavController().navigate(R.id.action_set_up_digital_wallet_form_fragment_to_country_code_selection)
        }

        binding.dateOfBirthView.setOnClickListener {
            DatePickerDialog(
                requireContext(), date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
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
        }

        if(model.countrySelected?.code == null){
            binding.root.snackbar("Select a country code")
            return
        }

        model.createVendor(
            binding.etEnterFirstNameInner.text.toString(),
            binding.etEnterLastNameInner.text.toString(),
            binding.etEnterEmailInner.text.toString(),
            "+441122334455"/*"+${model.countrySelected?.code}${binding.etEnterPhoneInner.text.toString()}"*/,
            "1995-01-01"/*binding.etEnterDOBInner.text.toString()*/
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

    private fun updateLabel() {
        val myFormat = "dd/MM/yy" //In which you need put here
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        binding.etEnterDOBInner.setText(sdf.format(myCalendar.time))
    }

}