package com.limor.app.scenes.auth_new.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.google.android.material.chip.Chip
import com.limor.app.R
import com.limor.app.extensions.hideKeyboard
import com.limor.app.scenes.auth_new.AuthActivityNew
import com.limor.app.scenes.auth_new.AuthViewModelNew
import com.limor.app.scenes.auth_new.data.Language
import com.limor.app.scenes.utils.BACKGROUND
import com.limor.app.scenes.utils.MAIN
import kotlinx.android.synthetic.main.fragment_new_auth_languages.*

class FragmentLanguages : Fragment() {

    private val model: AuthViewModelNew by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_auth_languages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListeners()
        setUpSearchEditText()
        subscribeToViewModel()
    }

    private fun setOnClickListeners() {
        btnContinue.setOnClickListener {
//            it.findNavController()
//                .navigate(R.id.)
        }

        topAppBar.setNavigationOnClickListener {
            it.findNavController().popBackStack()
        }

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

    private fun subscribeToViewModel() {
        model.downloadLanguages()
        model.languagesLiveData.observe(viewLifecycleOwner, Observer {
            createLanguagesArray(it)
        })

        model.languagesSelectionDone.observe(viewLifecycleOwner, Observer {
            btnContinue.isEnabled = it
        })
    }

    private fun createLanguagesArray(categories: List<Language>) {
        if (categories.isNotEmpty()) cgLanguages.removeAllViews()
        BACKGROUND({
            val chipsList = categories.map { category ->
                getVariantChip(category)
            }
            MAIN {
                chipsList.forEach { cgLanguages.addView(it) }
            }
        })
    }

    private fun getVariantChip(language: Language): Chip {
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