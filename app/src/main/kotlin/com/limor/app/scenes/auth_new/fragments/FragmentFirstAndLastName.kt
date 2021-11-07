package com.limor.app.scenes.auth_new.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.limor.app.R
import com.limor.app.scenes.auth_new.AuthActivityNew
import com.limor.app.scenes.auth_new.AuthViewModelNew
import com.limor.app.scenes.auth_new.navigation.AuthNavigator
import com.limor.app.scenes.auth_new.navigation.NavigationBreakpoints
import com.limor.app.scenes.auth_new.util.ToastMaker
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.fragment_first_and_last_name.*

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
        subscribeToViewModel()
        saveNavigationBreakPoint()
    }

    private fun subscribeToViewModel(){
        model.updateUserFirstNameAndLastNameLiveData.observe(viewLifecycleOwner, Observer{
            if (it == null) return@Observer
            AuthNavigator.navigateToFragmentByNavigationBreakpoints(
                requireActivity(),
                NavigationBreakpoints.USERNAME_CREATION.destination
            )
        })
        model.userInfoProviderErrorLiveData.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            ToastMaker.showToast(requireContext(), it)
        })
        model.currentUserFullNameIsValid.observe(viewLifecycleOwner, Observer {
            if(it == null) return@Observer
            btnNamesPickerContinue.isEnabled = it
        })
    }

    private fun setTextChangedListener() {
        etEnterFirstName.editText?.setText(model.firstName)
        etEnterFirstName.editText?.doAfterTextChanged { model.changeFirstName(it?.toString() ?: "") }
        etEnterLastName.editText?.setText(model.lastName)
        etEnterLastName.editText?.doAfterTextChanged { model.changeLastName(it?.toString() ?: "") }
    }

    private fun setOnClickListeners(){
        btnNamesPickerContinue.setOnClickListener {
            if(etEnterFirstName.editText?.text.isNullOrEmpty()){
                etEnterFirstName.error = "Required"
                etEnterFirstName.requestFocus()
            } else if(etEnterLastName.editText?.text.isNullOrEmpty()){
                etEnterLastName.error = "Required"
                etEnterLastName.requestFocus()
            } else {
                model.submitNames()
            }
        }
        btnNamesPickerBack.setOnClickListener {
            AuthActivityNew.popBackStack(requireActivity())
        }
    }

    private fun saveNavigationBreakPoint() {
        model.saveNavigationBreakPoint(
            requireContext(),
            NavigationBreakpoints.NAME_COLLECTION.destination
        )
    }

}