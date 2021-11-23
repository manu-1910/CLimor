package com.limor.app.scenes.patron.setup

import android.content.Context
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
import androidx.lifecycle.lifecycleScope
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
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_languages.btnContinue
import kotlinx.android.synthetic.main.fragment_languages.cgLanguages
import kotlinx.android.synthetic.main.fragment_languages.clMain
import kotlinx.android.synthetic.main.fragment_languages.etSearchLanguage
import kotlinx.android.synthetic.main.fragment_languages.topAppBar
import kotlinx.android.synthetic.main.fragment_patron_languages.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.jetbrains.anko.design.snackbar
import timber.log.Timber
import javax.inject.Inject

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
            if (publishViewModel.languageSelectedCodesList.isNotEmpty()) {
                lifecycleScope.launch {
                    switchCommonVisibility(true)
                    publishViewModel.addPatronLanguages().collect {
                        if (it == "Success") {
                            findNavController().navigate(R.id.action_fragmentPatronLanguages_to_fragmentPatronOnboardingSuccess)
                        } else {
                            btnContinue.snackbar(it!!)
                        }
                    }
                }
            }
        }
        val balloon = CommonsKt.createPopupBalloon(requireContext(),
            "You can only select 5 languages. if you speak english, Select `English`")
        btnLanguagesInfo.setOnClickListener {
            if (!balloon.isShowing) {
                it.showAlignBottom(balloon, 0, 0)
            } else {
                balloon.dismiss()
            }
        }
        topAppBar.setNavigationOnClickListener {
            /*if (activity?.intent?.getStringExtra("page") != null) {
                activity?.finish()
            } else {
                it.findNavController().popBackStack()
            }*/
            activity?.finish()

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
        cgLanguages.isSingleSelection = false
        chip.apply {
            text = language.name
            MAIN {
                isChecked = lastCheckedIds.contains(chip.id)
            }
            setOnCheckedChangeListener { buttonView, isChecked ->
                val ids: List<Int> = cgLanguages.checkedChipIds
                Timber.d("$isChecked")
                if (isChecked) {
                    language.isSelected = isChecked
                    //Get all checked chips in the group
                    if (ids.size > 5) {
                        chip.isChecked = false //force to unchecked the chip
                        chip.snackbar("You can only select 5 languages")
                    } else {
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
                btnContinue.isEnabled = ids.isNotEmpty()
            }
        }
        return chip
    }


}