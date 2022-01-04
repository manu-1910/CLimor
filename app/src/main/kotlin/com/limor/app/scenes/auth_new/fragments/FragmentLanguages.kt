package com.limor.app.scenes.auth_new.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import com.limor.app.R
import com.limor.app.extensions.hideKeyboard
import com.limor.app.scenes.auth_new.AuthActivityNew
import com.limor.app.scenes.auth_new.AuthViewModelNew
import com.limor.app.scenes.auth_new.data.LanguageWrapper
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.auth_new.util.ToastMaker
import com.limor.app.scenes.utils.MAIN
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_new_auth_languages.*
import javax.inject.Inject

class FragmentLanguages : DialogFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: AuthViewModelNew by activityViewModels { viewModelFactory }

    companion object {
        val TAG = FragmentLanguages::class.qualifiedName
        fun newInstance(): FragmentLanguages {
            return FragmentLanguages()
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
        return inflater.inflate(R.layout.fragment_new_auth_languages, container, false)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        );
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListeners()
        setUpSearchEditText()
        subscribeToViewModel()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    fun load() = model.downloadLanguages()

    val errorLiveData: LiveData<String>
        get() = model.languagesLiveDataError

    private fun setOnClickListeners() {
        btnContinue.setOnClickListener {
            model.updateLanguagesAndCategories()
        }

        /*topAppBar.setNavigationOnClickListener {
            dismiss()
        }*/

        clMain.setOnClickListener {
            clMain.requestFocus()
        }
        clMain.onFocusChangeListener = AuthActivityNew.onFocusChangeListener()
        cgLanguages.onFocusChangeListener = AuthActivityNew.onFocusChangeListener()
    }

    private fun setUpSearchEditText() {
        etSearchLanguage.editText?.doAfterTextChanged {
            model.onLanguageInputChanged(it?.toString())
        }
        etSearchLanguage.setEndIconOnClickListener {
            model.onLanguageInputChanged(etSearchLanguage.editText?.text?.toString())
            it.hideKeyboard()
        }
        etSearchLanguage.editText?.setOnEditorActionListener { textView, actionId, keyEvent ->
            when (actionId and EditorInfo.IME_MASK_ACTION) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    model.onLanguageInputChanged(etSearchLanguage.editText?.text?.toString())
                }
            }
            false
        }
    }

    fun subscribeToViewModel() {
        model.languagesLiveData.observe(viewLifecycleOwner, Observer {
            createLanguagesArray(it)
        })

        model.languagesSelectionDone.observe(viewLifecycleOwner, Observer {
            btnContinue.isEnabled = it
        })

        model.updatePreferredInfoLiveData.observe(viewLifecycleOwner, Observer {
            if (it == null)
                return@Observer
            PrefsHandler.setPreferencesSelected(requireContext(), true)
            dismiss()
        })

        model.userInfoProviderErrorLiveData.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            ToastMaker.showToast(requireContext(), it)
        })
    }

    private fun createLanguagesArray(languages: List<LanguageWrapper>) {
        cgLanguages.removeAllViews()
        val chipsList = languages.map { languageWrapper ->
            getVariantChip(languageWrapper)
        }
        chipsList.forEach { cgLanguages.addView(it) }
    }

    private fun getVariantChip(language: LanguageWrapper): Chip {
        val chip = layoutInflater.inflate(R.layout.item_chip_category, null) as Chip
        chip.apply {
            text = language.name
            MAIN {
                isChecked = language.isSelected
            }
            setOnCheckedChangeListener { buttonView, isChecked ->
                language.isSelected = isChecked
                model.updateLanguagesSelection()
                buttonView.hideKeyboard()
            }
        }
        return chip
    }
}