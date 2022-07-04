package com.limor.app.scenes.auth_new.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.limor.app.R
import com.limor.app.scenes.auth_new.AuthActivityNew
import com.limor.app.scenes.auth_new.AuthViewModelNew
import com.limor.app.scenes.auth_new.data.UserNameState
import com.limor.app.scenes.auth_new.data.UserNameStateBundle
import com.limor.app.scenes.auth_new.navigation.AuthNavigator
import com.limor.app.scenes.auth_new.navigation.NavigationBreakpoints
import com.limor.app.scenes.auth_new.util.ToastMaker
import com.limor.app.scenes.auth_new.util.colorStateList
import com.limor.app.scenes.utils.AlphabetsInputFilter
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.fragment_first_and_last_name.*
import kotlinx.android.synthetic.main.fragment_first_and_last_name.etEnterUsername
import kotlinx.android.synthetic.main.fragment_new_auth_enter_username.*

class FragmentFirstAndLastName : Fragment() {

    private val model: AuthViewModelNew by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_first_and_last_name, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListeners()
        setTextChangedListener()
        setHelperTexts()
        setFocusListeners()
        subscribeToViewModel()
        saveNavigationBreakPoint()
    }

    private fun subscribeToViewModel() {
        model.updateUserFirstNameAndLastNameLiveData.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            model.saveNavigationBreakPoint(
                requireContext(),
                NavigationBreakpoints.USERNAME_CREATION.destination
            )
            model.submitUsername(etEnterUsername?.editText?.text?.toString())
        })
        model.userInfoProviderErrorLiveData.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            ToastMaker.showToast(requireContext(), it)
            btnNamesPickerContinue.isEnabled = true
        })
        model.currentUserFullNameIsValid.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            if(model.currentUsernameIsValid.value == true)
                btnNamesPickerContinue.isEnabled = it
        })
        model.currentUsernameIsValid.observe(viewLifecycleOwner, Observer<Boolean> {
            if(model.currentUserFullNameIsValid.value == true)
                btnNamesPickerContinue.isEnabled = it
        })
        model.currentUsernameState.observe(viewLifecycleOwner, Observer<UserNameStateBundle> {
            when (it.state) {
                UserNameState.Editing -> {
                    actionUsernameStateEditing()
                }
                UserNameState.Error -> {
                    actionUsernameStateError(it)
                }
                UserNameState.Approved -> {
                    actionUsernameStateApproved()
                }
            }
        })
        model.userNameAttachedToUserLiveData.observe(viewLifecycleOwner, Observer {
            if (it != true) return@Observer
            model.updateUserName()
        })

        model.updateUserNameLiveData.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            etEnterUsername.isErrorEnabled = true
            etEnterUsername.error = "You're good to go"
            etEnterUsername.boxStrokeErrorColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.approved_green))
            etEnterUsername.setErrorTextColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.approved_green)))
            etEnterUsername.hintTextColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.approved_green))
            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                etEnterUsername.invalidate()
            }, 100)
            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                findNavController().navigate(R.id.action_fragment_new_auth_first_last_name_to_fragment_new_auth_suggested_people)
            }, 3000)
        })
    }

    private fun setHelperTexts(){
        etEnterFirstName.helperText = getString(R.string.required)
        etEnterLastName.helperText = getString(R.string.required)
        etEnterUsername.helperText = getString(R.string.required)
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            etEnterUsername.error = null
            etEnterUsername.helperText = getString(R.string.required)
        }, 200)
    }

    private fun setTextChangedListener() {
        etEnterFirstName.editText?.setText(model.firstName)
        etEnterFirstName.editText?.doAfterTextChanged {
            model.changeFirstName(
                it?.toString() ?: ""
            )
        }
        etEnterLastName.editText?.setText(model.lastName)
        etEnterLastName.editText?.doAfterTextChanged { model.changeLastName(it?.toString() ?: "") }

        etEnterFirstName.editText?.filters = arrayOf(AlphabetsInputFilter())
        etEnterLastName.editText?.filters = arrayOf(AlphabetsInputFilter())

        etEnterUsername.editText?.setText(model.currentUsername)
        etEnterUsername.editText?.doAfterTextChanged { model.changeCurrentUserName(it?.toString()) }
    }

    private fun setFocusListeners(){
        etEnterFirstNameInner.setOnFocusChangeListener { view, b ->
            if(!b){
                if(etEnterFirstNameInner.text.toString().isEmpty()){
                    etEnterFirstName.error = getString(R.string.cant_be_empty)
                } else{
                    etEnterFirstName.error = null
                    etEnterFirstName.helperText = getString(R.string.required)
                }
            } else{
                etEnterFirstName.error = null
                etEnterFirstName.helperText = getString(R.string.required)
            }
        }
        etEnterLastNameInner.setOnFocusChangeListener { view, b ->
            if(!b){
                if(etEnterLastNameInner.text.toString().isEmpty()){
                    etEnterLastName.error = getString(R.string.cant_be_empty)
                } else{
                    etEnterLastName.error = null
                    etEnterLastName.helperText = getString(R.string.required)
                }
            } else{
                etEnterLastName.error = null
                etEnterLastName.helperText = getString(R.string.required)
            }
        }
        etEnterUsernameInner.setOnFocusChangeListener { view, b ->
            if(!b){
                if(etEnterUsernameInner.text.toString().isEmpty()){
                    etEnterUsername.error = getString(R.string.cant_be_empty)
                } else{
                    etEnterUsername.error = null
                    etEnterUsername.helperText = getString(R.string.required)
                }
            } else{
                etEnterUsername.error = null
                etEnterUsername.helperText = getString(R.string.required)
            }
        }
    }

    private fun setOnClickListeners() {
        btnNamesPickerContinue.setOnClickListener {
            if (etEnterFirstName.editText?.text.isNullOrEmpty()) {
                etEnterFirstName.error = "Required"
                etEnterFirstName.requestFocus()
            } else if (etEnterLastName.editText?.text.isNullOrEmpty()) {
                etEnterLastName.error = "Required"
                etEnterLastName.requestFocus()
            } else if (etEnterUsername.editText?.text.isNullOrEmpty()){
                etEnterUsername.error = "Required"
                etEnterUsername.requestFocus()
            } else {
                btnNamesPickerContinue.isEnabled = false
                model.submitNames()
            }
        }
        etEnterUsername.setEndIconOnClickListener {
            model.changeCurrentUserName("")
            etEnterUsername.editText?.setText("")
        }
        btnNamesPickerBack.setOnClickListener {
            AuthActivityNew.popBackStack(requireActivity())
        }
    }

    private fun actionUsernameStateApproved() {
        btnContinue.requestFocus()
        etEnterUsername.error = null
        etEnterUsername.setBoxStrokeColorStateList(
            colorStateList(requireContext(), R.color.text_edit_outline_color_approved)
        )
        etEnterUsername.helperText = getString(R.string.you_are_good_to_go)
        etEnterUsername.setHelperTextColor(
            colorStateList(requireContext(), R.color.text_edit_outline_color_approved)
        )
        etEnterUsername.hintTextColor =
            colorStateList(requireContext(), R.color.text_edit_outline_color_approved)
        userNameVariants.isVisible = false
    }

    private fun actionUsernameStateError(it: UserNameStateBundle) {
        btnContinue.isEnabled = false
        etEnterUsername.helperText = null
        etEnterUsername.error = getString(R.string.username_already_in_use)
        if (it.params?.size ?: 0 != 0) userNameVariants.removeAllViews()
        it.params?.forEach { variant ->
            if (variant == null || variant.isEmpty()) return@forEach
            val chip = getVariantChip(variant)
            userNameVariants.addView(chip)
        }
        userNameVariants.isVisible = true
    }

    private fun getVariantChip(variant: String?): Chip {
        val chip = layoutInflater.inflate(R.layout.item_chip_username, null) as Chip
        chip.text = variant
        chip.setOnClickListener {
            etEnterUsername.editText?.setText(variant)
            model.changeCurrentUserName(variant, fromVariant = true)
        }
        return chip
    }

    private fun actionUsernameStateEditing() {
        etEnterUsername.error = null
        etEnterUsername.helperText = null
        etEnterUsername.setBoxStrokeColorStateList(
            colorStateList(requireContext(), R.color.text_edit_outline_color)
        )
        etEnterUsername.hintTextColor =
            colorStateList(requireContext(), R.color.text_edit_outline_color)
        userNameVariants.isVisible = false
    }

    private fun saveNavigationBreakPoint() {
        model.saveNavigationBreakPoint(
            requireContext(),
            NavigationBreakpoints.NAME_COLLECTION.destination
        )
    }

}