package com.limor.app.scenes.main.fragments.record


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.limor.app.R
import com.limor.app.di.Injectable
import com.limor.app.extensions.hideKeyboard
import com.limor.app.scenes.auth_new.data.LanguageWrapper
import com.limor.app.scenes.auth_new.fragments.FragmentWithLoading
import com.limor.app.scenes.main.viewmodels.LanguagesViewModel
import com.limor.app.scenes.main.viewmodels.PublishViewModel
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.scenes.utils.MAIN
import com.skydoves.balloon.showAlignBottom
import kotlinx.android.synthetic.main.fragment_languages.*
import kotlinx.android.synthetic.main.fragment_languages.btnContinue
import kotlinx.android.synthetic.main.fragment_languages.clMain
import kotlinx.android.synthetic.main.fragment_languages.topAppBar
import kotlinx.android.synthetic.main.fragment_publish_categories.*
import timber.log.Timber
import javax.inject.Inject


class LanguagesFragment : FragmentWithLoading(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: LanguagesViewModel by activityViewModels { viewModelFactory }
    private val publishViewModel: PublishViewModel by activityViewModels { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_languages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListeners()
        setUpSearchEditText()
    }

    override fun load() = model.downloadLanguages()
    var lastCheckedId = View.NO_ID

    override val errorLiveData: LiveData<String>
        get() = model.languagesLiveDataError

    private fun setOnClickListeners() {
        btnContinue.setOnClickListener {
            if (publishViewModel.languageSelected.isNotEmpty()) {
                findNavController().popBackStack()
            }
        }

        topAppBar.setNavigationOnClickListener {
            it.findNavController().popBackStack()
        }

        clMain.setOnClickListener {
            clMain.requestFocus()
        }
        //clMain.onFocusChangeListener = AuthActivityNew.onFocusChangeListener()
        //cgLanguages.onFocusChangeListener = AuthActivityNew.onFocusChangeListener()
        val balloon = CommonsKt.createPopupBalloon(
            requireContext(),
            getString(R.string.languages_hint)
        )
        languagesInfoBtn.setOnClickListener {
            balloon.showAlignBottom(it)
            if (!balloon.isShowing) {
                it.showAlignBottom(balloon, 0, 0)
            } else {
                balloon.dismiss()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        model.onLanguageInputChanged(null)
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

    override fun subscribeToViewModel() {
        super.subscribeToViewModel()
        model.languagesLiveData.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty())
                switchCommonVisibility()
            createLanguagesArray(it)
        })

        model.languagesSelectionDone.observe(viewLifecycleOwner, Observer {
            btnContinue.isEnabled = it
        })
    }

    private fun createLanguagesArray(languages: List<LanguageWrapper>) {
        cgLanguages.removeAllViews()
        cgLanguages.isSingleSelection = true
        val chipsList = languages.map { languageWrapper ->
            getVariantChip(languageWrapper)
        }
        chipsList.forEach {
            it.id = View.generateViewId()
            cgLanguages.addView(it)
        }
    }

    private fun getVariantChip(language: LanguageWrapper): Chip {
        val chip = layoutInflater.inflate(R.layout.item_chip_category, null) as Chip
        chip.apply {
            text = language.name
            MAIN {
                isChecked = (publishViewModel.languageSelected == language.name)
            }
            setOnCheckedChangeListener { buttonView, isChecked ->

                if (isChecked) {
                    language.isSelected = isChecked
                    lastCheckedId = chip.id
                    publishViewModel.languageSelected = text.toString()
                    publishViewModel.languageCode = language.code
                } else {
                    language.isSelected = false
                    lastCheckedId = View.NO_ID
                }
                Timber.d("Chip -> ${language.code} -- ${language.name}")
                model.updateLanguagesSelection()
            }
        }
        return chip
    }
}

