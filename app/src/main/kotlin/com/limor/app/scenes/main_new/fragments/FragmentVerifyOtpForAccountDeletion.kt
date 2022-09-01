package com.limor.app.scenes.main_new.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.limor.app.App
import com.limor.app.R
import com.limor.app.extensions.hideKeyboard
import com.limor.app.scenes.auth_new.AuthViewModelNew
import com.limor.app.scenes.auth_new.firebase.FirebaseSessionHandler
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.main.fragments.settings.SettingsViewModel
import com.limor.app.scenes.splash.SplashActivity
import com.limor.app.scenes.utils.LimorDialog
import com.onesignal.OneSignal
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_delete_account_phone_number_input.*
import kotlinx.android.synthetic.main.fragment_new_auth_phone_code.*
import kotlinx.android.synthetic.main.fragment_verify_otp_for_account_deletion.*
import kotlinx.android.synthetic.main.fragment_verify_otp_for_account_deletion.btnContinue
import kotlinx.android.synthetic.main.fragment_verify_otp_for_account_deletion.etSms1
import kotlinx.android.synthetic.main.fragment_verify_otp_for_account_deletion.etSms2
import kotlinx.android.synthetic.main.fragment_verify_otp_for_account_deletion.etSms3
import kotlinx.android.synthetic.main.fragment_verify_otp_for_account_deletion.etSms4
import kotlinx.android.synthetic.main.fragment_verify_otp_for_account_deletion.etSms5
import kotlinx.android.synthetic.main.fragment_verify_otp_for_account_deletion.etSms6
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.android.material.snackbar.Snackbar


class FragmentVerifyOtpForAccountDeletion : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: AuthViewModelNew by activityViewModels { viewModelFactory }
    private val settingsViewModel: SettingsViewModel by activityViewModels { viewModelFactory }

    private val smsCodeEtList = mutableListOf<TextInputLayout>()

    private var coroutineJob: Job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + coroutineJob)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_verify_otp_for_account_deletion, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingsViewModel.showLogoInToolbar(true)
        initialiseUI()
        createSmsCodeFieldsList()
        setClickListeners()
        setTextChangeListener()
        subscribeToViewModel()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    private fun initialiseUI() {
        phoneNumberTV.text = model.formattedPhone
    }

    private fun createSmsCodeFieldsList() {
        smsCodeEtList.add(etSms1)
        smsCodeEtList.add(etSms2)
        smsCodeEtList.add(etSms3)
        smsCodeEtList.add(etSms4)
        smsCodeEtList.add(etSms5)
        smsCodeEtList.add(etSms6)
    }

    private fun setClickListeners() {
        btnContinue.setOnClickListener {
            LimorDialog(layoutInflater).apply {
                setTitle("Delete your Limor account?")
                addButton(R.string.cancel, true) {
                    dismiss()
                }
                addButton(R.string.delete, false) {
                    model.validateOtpToDeleteUserAccount(smsCodesList())
                }
            }.show()
        }
        resendTV.setOnClickListener {
            model.sendOtpToDeleteUserAccount()
        }
    }

    private fun setTextChangeListener() {
        for (et in smsCodeEtList) {
            val currentIndex = smsCodeEtList.indexOf(et)
            setOnTextChangedListener(et.editText!!, currentIndex)
            setOnDoneActionClicked(et.editText!!, currentIndex)
        }
    }

    private fun setOnTextChangedListener(et: EditText, currentIndex: Int) {
        et.doOnTextChanged { inputText, _, _, _ ->
            model.setSmsCodeForCheck(smsCodesList())

            if (inputText?.isNotEmpty() ?: false) {
                //user entered symbol
                if (currentIndex != smsCodeEtList.size - 1)
                    smsCodeEtList[currentIndex + 1].requestFocus()
            } else {
                if (currentIndex != 0)
                    smsCodeEtList[currentIndex - 1].requestFocus()
            }
        }
        et.doAfterTextChanged {
            resetErrorMessages()
        }
    }

    private fun validateSmsCode() {
        btnContinue.isEnabled = false
        context?.resources?.let { btnContinue.setBackgroundColor(it.getColor(R.color.bg_grey)) }
        btnContinue.hideKeyboard()
        resetErrorMessages()
        model.validateOtpToDeleteUserAccount(smsCodesList())
    }

    private fun subscribeToViewModel() {
        model.otpValidToDeleteUser.observe(viewLifecycleOwner, {
            if (it == null) {
                return@observe
            }
            verifyLayout.visibility = View.GONE
            viewAccountDeleted.visibility = View.VISIBLE
            settingsViewModel.showToolbar(false)
            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                logout()
            }, 5000)
        })
        model.otpInValid.observe(viewLifecycleOwner, {
            if (it == null) {
                return@observe
            }
            model.cancelTimers()
            smsCodeEtList.forEach { et ->
                et.error = " "
                et.editText!!.setTextColor(
                    resources
                        .getColor(R.color.error_stroke_color)
                )
            }
            // re-enable the button as the user might want to enter a new OTP
            btnContinue.isEnabled = false
            context?.resources?.let { btnContinue.setBackgroundColor(it.getColor(R.color.bg_grey)) }
            showErrorInSnackBar(it)
        })
        model.resendButtonCountDownLiveData.observe(viewLifecycleOwner, {
            if (it == null) {
                resendTV.setText(R.string.didn_t_get_the_code_resend_it)
                resendTV.setTextColor(resources.getColor(R.color.blue500))
            } else {
                resendTV.text =
                    getString(R.string.resend_code_in) + (if (it < 10) " 0$it seconds" else " $it seconds")
                resendTV.setTextColor(resources.getColor(R.color.ef_grey))
            }
        })
        model.resendButtonEnableLiveData.observe(viewLifecycleOwner, {
            resendTV.isEnabled = it
            if (!it) {
                resendTV.setText(R.string.didn_t_get_the_code_resend_it)
            }
        })
        model.otpSentToDeleteUser.observe(viewLifecycleOwner, {
            if (it == null) {
                return@observe
            }
            if (it.toString().lowercase() == "success") {
                model.enableResend()
                Toast.makeText(activity, "Code has been sent", Toast.LENGTH_LONG)
                    .show()
            } else {
                showErrorInSnackBar(it)
            }
        })
        model.smsContinueButtonEnabled.observe(viewLifecycleOwner, Observer {
            if (it == null)
                return@Observer
            btnContinue.isEnabled = it
            if (it == true) {
                context?.resources?.let { btnContinue.setBackgroundColor(it.getColor(R.color.red600)) }
            } else {
                context?.resources?.let { btnContinue.setBackgroundColor(it.getColor(R.color.bg_grey)) }
            }
        })
    }

    private fun showErrorInSnackBar(errorMessage: String) {
        Snackbar.make(parentLayout, errorMessage, Snackbar.LENGTH_SHORT)
            .setTextColor(resources.getColor(android.R.color.white))
            .show()
    }

    private fun smsCodesList(): List<String?> = smsCodeEtList.map { it.editText!!.text?.toString() }

    private fun setOnDoneActionClicked(et: EditText, currentIndex: Int) {
        et.setOnEditorActionListener { textView, actionId, keyEvent ->
            when (actionId and EditorInfo.IME_MASK_ACTION) {
                EditorInfo.IME_ACTION_DONE -> {
                    model.setSmsCodeForCheck(smsCodesList())
                }

                EditorInfo.IME_ACTION_NEXT -> {
                    if (currentIndex != smsCodeEtList.size - 1)
                        smsCodeEtList[currentIndex + 1].requestFocus()
                }
            }
            false
        }
    }

    private fun resetErrorMessages() {
        //tvWrongCode.visibility = View.GONE
        //tvWrongCode.text = ""
        smsCodeEtList.forEach { et ->
            et.error = null
            et.editText?.setTextColor(ContextCompat.getColor(et.context, R.color.black))
        }
    }

    private fun logout() {
        lifecycleScope.launch {
            scope.launch {
                App.instance.chatManager.logout()
                settingsViewModel.deleteUserDevice()
                OneSignal.removeExternalUserId()
            }
            try {
                FirebaseSessionHandler.logout(requireContext())
                Toast.makeText(requireContext(), "Done!", Toast.LENGTH_LONG).show()
                PrefsHandler.setCanShowCategorySelection(requireContext(), false)
                PrefsHandler.setPreferencesSelected(requireContext(), false)
                PrefsHandler.setPreferencesScreenOpenedInThisSession(requireContext(), false)
                PrefsHandler.setCanShowGenderSelection(requireContext(), false)
                (activity)?.finishAffinity()
                startActivity(
                    Intent(requireContext(), SplashActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error -> ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

}