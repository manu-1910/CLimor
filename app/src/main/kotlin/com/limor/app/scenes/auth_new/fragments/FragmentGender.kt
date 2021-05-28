package com.limor.app.scenes.auth_new.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.limor.app.GendersQuery
import com.limor.app.R
import com.limor.app.scenes.auth_new.AuthActivityNew
import com.limor.app.scenes.auth_new.AuthViewModelNew
import kotlinx.android.synthetic.main.fragment_new_auth_gender.*

class FragmentGender : FragmentWithLoading() {

    private val model: AuthViewModelNew by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_auth_gender, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListeners()
        toggleGender.isEnabled = false
    }

    override fun load() {
        model.downloadGenders()
    }

    override val errorLiveData: LiveData<String>
        get() = model.gendersLiveDataError


    override fun subscribeToViewModel() {
        super.subscribeToViewModel()
        model.gendersSelectionDone.observe(viewLifecycleOwner, Observer {
            btnContinue.isEnabled = it
        })

        model.gendersLiveData.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                switchCommonVisibility()
                setUpToggleButton(it)
            }
        })
    }

    private fun setOnClickListeners() {
        btnContinue.setOnClickListener {
            it.findNavController()
                .navigate(R.id.action_fragment_new_auth_gender_to_fragment_new_auth_categories)
        }

        btnBack.setOnClickListener {
            AuthActivityNew.popBackStack(requireActivity())
        }

        btnSkip.setOnClickListener {
            model.selectGender(0)
            it.findNavController()
                .navigate(R.id.action_fragment_new_auth_gender_to_fragment_new_auth_categories)
        }
    }

    private fun setUpToggleButton(list: List<GendersQuery.Gender>) {
        addToggleClickListener(list)
        setUpInitialGender()
    }

    private fun addToggleClickListener(list: List<GendersQuery.Gender>) {
        btnGender1.text = list[0].gender
        btnGender2.text = list[1].gender
        btnGender3.text = list[2].gender
        toggleGender.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            if (isChecked) {
                val gender = when (checkedId) {
                    R.id.btnGender1 -> list[0]
                    R.id.btnGender2 -> list[1]
                    R.id.btnGender3 -> list[2]
                    else -> list[0]
                }
                model.selectGender(gender.id ?: 0)
            }
        }
    }

    private fun setUpInitialGender() {
        val checkedId = when (model.selectedGenderIndex) {
            0 -> R.id.btnGender1
            1 -> R.id.btnGender2
            2 -> R.id.btnGender3
            else -> R.id.btnGender1
        }
        toggleGender.check(checkedId)
    }
}