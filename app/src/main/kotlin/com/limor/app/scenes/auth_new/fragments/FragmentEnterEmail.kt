package com.limor.app.scenes.auth_new.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.limor.app.R
import com.limor.app.scenes.auth_new.AuthActivityNew
import com.limor.app.scenes.auth_new.AuthViewModelNew
import com.limor.app.scenes.auth_new.navigation.NavigationBreakpoints
import kotlinx.android.synthetic.main.fragment_new_auth_enter_email.*
import timber.log.Timber

class FragmentEnterEmail : Fragment() {

    private val model: AuthViewModelNew by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_auth_enter_email, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListeners()
        setEditTextChangedListeners()
        subscribeToViewModel()
        saveNavigationBreakPoint()
    }

    private fun setOnClickListeners() {
        btnContinue.setOnClickListener {
            model.checkEmailIsInUse()
        }

        btnBack.setOnClickListener {
            AuthActivityNew.popBackStack(requireActivity())
        }

        etEnterEmail.setEndIconOnClickListener {
            model.changeCurrentEmail("")
            etEnterEmail.editText?.setText("")
        }
        clMain.setOnClickListener {
            clMain.requestFocus()
        }
        clMain.onFocusChangeListener = AuthActivityNew.onFocusChangeListener()
    }

    private fun setEditTextChangedListeners() {
        etEnterEmail.requestFocus()
        etEnterEmail.editText?.setText(model.currentEmail)
        etEnterEmail.editText?.doAfterTextChanged { model.changeCurrentEmail(it?.toString()) }
    }

    private fun subscribeToViewModel() {
        model.currentEmailIsValidLiveData.observe(viewLifecycleOwner, Observer<Boolean> {
            btnContinue.isEnabled = it
        })

        model.currentEmailIsInUseLiveData.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            if (it) {
                Toast.makeText(
                    requireContext(),
                    R.string.email_is_already_in_use,
                    Toast.LENGTH_LONG
                ).show()
                return@Observer
            }
            Timber.d("currentEmailIsInUseLiveData -> $it")
            model.addEmailToUserAccount()

        })

        model.emailIsAttachedToUserLiveData.observe(viewLifecycleOwner, Observer {
            if (it != true) return@Observer
            clMain.findNavController()
                .navigate(R.id.action_fragment_new_auth_enter_email_to_fragment_new_auth_enter_username)
        })

        model.emailAuthHandlerErrorLiveData.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            Toast.makeText(
                requireContext(),
                it,
                Toast.LENGTH_LONG
            ).show()
        })
    }

    private fun saveNavigationBreakPoint() {
        model.saveNavigationBreakPoint(requireContext(), NavigationBreakpoints.ACCOUNT_CREATION.destination)
    }
}