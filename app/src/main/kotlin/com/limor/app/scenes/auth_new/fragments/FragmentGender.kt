package com.limor.app.scenes.auth_new.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.limor.app.GendersQuery
import com.limor.app.R
import com.limor.app.scenes.auth_new.AuthViewModelNew
import com.limor.app.scenes.auth_new.util.PrefsHandler
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_new_auth_gender.*
import javax.inject.Inject

class FragmentGender : DialogFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: AuthViewModelNew by activityViewModels { viewModelFactory }

    companion object {
        val TAG = FragmentGender::class.qualifiedName
        fun newInstance(): FragmentGender {
            return FragmentGender()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_AppCompat_FloatingDialog)
        load()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_auth_gender, container, false)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListeners()
        subscribeToViewModel()
        toggleGender.isEnabled = false
    }

    fun load() {
        model.downloadGenders()
    }

    val errorLiveData: LiveData<String>
        get() = model.gendersLiveDataError


    fun subscribeToViewModel() {
        model.gendersSelectionDone.observe(viewLifecycleOwner, Observer {
            btnContinue.isEnabled = it
        })

        model.gendersLiveData.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                setUpToggleButton(it)
            }
        })
        model.updatePreferredInfoLiveData.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            PrefsHandler.setCanShowGenderSelection(requireContext(), false)
            dismiss()
        })
    }

    private fun setOnClickListeners() {
        btnContinue.setOnClickListener {
            model.updateGenderInfo()
        }
    }

    private fun setUpToggleButton(list: List<GendersQuery.Gender>) {
        addToggleClickListener(list)
        setUpInitialGender()
    }

    private fun addToggleClickListener(list: List<GendersQuery.Gender>) {
        btGender1.text = list[0].gender
        btGender2.text = list[1].gender
        btGender3.text = list[2].gender
        toggleGender.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            if (isChecked) {
                val gender = when (checkedId) {
                    R.id.btGender1 -> list[0]
                    R.id.btGender2 -> list[1]
                    R.id.btGender3 -> list[2]
                    else -> list[0]
                }
                model.selectGender(gender.id ?: 0)
            }
        }
    }

    private fun setUpInitialGender() {
        val checkedId = when (model.selectedGenderIndex) {
            0 -> R.id.btGender1
            1 -> R.id.btGender2
            2 -> R.id.btGender3
            else -> R.id.btGender1
        }
        toggleGender.check(checkedId)
    }

}