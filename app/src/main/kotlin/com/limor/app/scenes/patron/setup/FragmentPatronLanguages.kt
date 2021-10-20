package com.limor.app.scenes.patron.setup

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
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
import com.limor.app.scenes.utils.MAIN
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_languages.*
import kotlinx.android.synthetic.main.fragment_languages.btnContinue
import kotlinx.android.synthetic.main.fragment_languages.clMain
import kotlinx.android.synthetic.main.fragment_languages.topAppBar
import kotlinx.android.synthetic.main.fragment_publish_categories.*
import org.jetbrains.anko.design.snackbar
import timber.log.Timber
import javax.inject.Inject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class FragmentPatronLanguages : FragmentWithLoading(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: LanguagesViewModel by activityViewModels { viewModelFactory }
    private val publishViewModel: PublishViewModel by activityViewModels { viewModelFactory }

    var lastCheckedIds = hashSetOf<Int>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_patron_languages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListeners()
        setUpSearchEditText()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) = FragmentPatronLanguages()
    }

    override fun load() = model.downloadLanguages()
    var lastCheckedId = View.NO_ID

    override val errorLiveData: LiveData<String>
        get() = model.languagesLiveDataError

    private fun setOnClickListeners() {
        btnContinue.setOnClickListener {
            if (publishViewModel.languageSelected.isNotEmpty()) {
                findNavController().navigate(R.id.action_fragmentPatronLanguages_to_fragmentPatronOnboardingSuccess)
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
    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }
    private fun getVariantChip(language: LanguageWrapper): Chip {
        val chip = layoutInflater.inflate(R.layout.item_chip_category, null) as Chip
        chip.apply {
            text = language.name
            MAIN {
                isChecked =  lastCheckedIds.contains(chip.id)
            }
            setOnCheckedChangeListener { buttonView, isChecked ->
                val ids: List<Int> = cgCategories.checkedChipIds
                Timber.d("$isChecked")
                if (isChecked) {
                    language.isSelected = isChecked
                    //Get all checked chips in the group
                    if (ids.size > 5) {
                        chip.isChecked = false //force to unchecked the chip
                        chip.snackbar("You can only select 5 categories")
                    }else{
                       // lastCheckedIds.add(chip.id)
                        language.language.code?.let {
                            //publishViewModel.categorySelectedId = it
                            publishViewModel.languageSelectedCodesList.add(it)
                        }
                    }
                } else {
                    //lastCheckedId = View.NO_ID
                    lastCheckedIds.remove(chip.id)
                    chip.isChecked = false
                    publishViewModel.languageSelectedCodesList.remove(language.code)
                    // category.isSelected = false
                    //model.updateCategoriesSelection()
                }
                btnContinue.isEnabled = ids.size>=5
            }
        }
        return chip
    }


}