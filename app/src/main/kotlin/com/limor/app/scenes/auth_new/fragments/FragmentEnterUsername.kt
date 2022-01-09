package com.limor.app.scenes.auth_new.fragments

import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.google.android.material.chip.Chip
import com.limor.app.R
import com.limor.app.scenes.auth_new.AuthActivityNew
import com.limor.app.scenes.auth_new.AuthViewModelNew
import com.limor.app.scenes.auth_new.data.UserNameState
import com.limor.app.scenes.auth_new.data.UserNameStateBundle
import com.limor.app.scenes.auth_new.navigation.AuthNavigator
import com.limor.app.scenes.auth_new.navigation.NavigationBreakpoints
import com.limor.app.scenes.auth_new.util.ToastMaker.Companion.showToast
import com.limor.app.scenes.auth_new.util.colorStateList
import kotlinx.android.synthetic.main.fragment_new_auth_enter_username.*

class FragmentEnterUsername : Fragment() {

    private val model: AuthViewModelNew by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_auth_enter_username, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListeners()
        setEditTexts()
        subscribeToViewModel()
        saveNavigationBreakPoint()
    }

    private fun setOnClickListeners() {
        btnContinue.setOnClickListener {
            model.submitUsername(etEnterUsername?.editText?.text?.toString())
        }

        etEnterUsername.setEndIconOnClickListener {
            model.changeCurrentUserName("")
            etEnterUsername.editText?.setText("")
        }
        clMain.setOnClickListener {
            clMain.requestFocus()
        }
        clMain.onFocusChangeListener = AuthActivityNew.onFocusChangeListener()
    }

    private fun setEditTexts() {
        etEnterUsername.editText?.setText(model.currentUsername)
        etEnterUsername.editText?.doAfterTextChanged { model.changeCurrentUserName(it?.toString()) }

        val spacesFilter = InputFilter { charSequence, _, _, _, _, _ ->
            charSequence.toString().trim().replace("\\s+".toRegex(), replacement = "")
        }
        etDobPickerInner.apply {
            filters = filters.plus(spacesFilter)
        }
    }

    private fun subscribeToViewModel() {
        model.currentUsernameIsValid.observe(viewLifecycleOwner, Observer<Boolean> {
            btnContinue.isEnabled = it
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

        model.userInfoProviderErrorLiveData.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            showToast(requireContext(), it)
        })

        model.userNameAttachedToUserLiveData.observe(viewLifecycleOwner, Observer {
            if (it != true) return@Observer
            model.updateUserName()
        })

        model.updateUserNameLiveData.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            AuthNavigator.navigateToFragmentByNavigationBreakpoints(
                requireActivity(),
                NavigationBreakpoints.PREFERENCE_COLLECTION.destination
            )
        })
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
        cgVariants.isVisible = false
    }

    private fun actionUsernameStateError(it: UserNameStateBundle) {
        btnContinue.isEnabled = false
        etEnterUsername.helperText = null
        etEnterUsername.error = getString(R.string.username_already_in_use)
        if (it.params?.size ?: 0 != 0) cgVariants.removeAllViews()
        it.params?.forEach { variant ->
            if (variant == null || variant.isEmpty()) return@forEach
            val chip = getVariantChip(variant)
            cgVariants.addView(chip)
        }
        cgVariants.isVisible = true
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
        cgVariants.isVisible = false
    }

    private fun saveNavigationBreakPoint() {
        model.saveNavigationBreakPoint(
            requireContext(),
            NavigationBreakpoints.USERNAME_CREATION.destination
        )
    }
}