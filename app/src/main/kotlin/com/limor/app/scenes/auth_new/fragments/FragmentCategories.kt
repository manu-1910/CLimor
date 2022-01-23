package com.limor.app.scenes.auth_new.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import com.limor.app.R
import com.limor.app.scenes.auth_new.AuthViewModelNew
import com.limor.app.scenes.auth_new.data.CategoryWrapper
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.utils.BACKGROUND
import com.limor.app.scenes.utils.MAIN
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_new_auth_categories.*
import javax.inject.Inject


class FragmentCategories : DialogFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: AuthViewModelNew by activityViewModels { viewModelFactory }

    companion object {
        val TAG = FragmentCategories::class.qualifiedName
        fun newInstance(): FragmentCategories {
            return FragmentCategories()
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
        return inflater.inflate(R.layout.fragment_new_auth_categories, container, false)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListeners()
        subscribeToViewModel()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    fun load() = model.downloadCategories()

    val errorLiveData: LiveData<String>
        get() = model.categoryLiveDataError

    fun subscribeToViewModel() {
        model.categoriesLiveData.observe(viewLifecycleOwner, Observer {
            if (it.isEmpty())
                return@Observer
            createCategoriesArray(it)
        })

        model.categorySelectionDone.observe(viewLifecycleOwner, Observer {
            btnContinue.isEnabled = it
        })

        model.updatePreferredInfoLiveData.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            PrefsHandler.setPreferencesSelected(requireContext(), true)
            dismiss()
        })
    }

    private fun createCategoriesArray(categories: List<CategoryWrapper>) {
        if (categories.isNotEmpty()) cgCategories.removeAllViews()
        BACKGROUND({
            val categoriesChips =
                categories.map { category ->
                    getVariantChip(category)
                }
            MAIN { categoriesChips.forEach { cgCategories.addView(it) } }
        })
    }

    private fun getVariantChip(category: CategoryWrapper): Chip {
        val chip = layoutInflater.inflate(R.layout.item_chip_category, null) as Chip
        chip.text = category.name
        MAIN {
            chip.isChecked = category.isSelected
        }
        chip.setOnCheckedChangeListener { buttonView, isChecked ->
            category.isSelected = isChecked
            model.updateCategoriesSelection()
        }
        return chip
    }

    private fun setOnClickListeners() {
        btnContinue.setOnClickListener {
            val dialog = FragmentLanguages.newInstance()
            dialog.show(parentFragmentManager, FragmentLanguages.TAG)
            dismiss()
        }
    }
}