package com.limor.app.scenes.auth_new.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.google.android.material.chip.Chip
import com.limor.app.R
import com.limor.app.scenes.auth_new.AuthViewModelNew
import com.limor.app.scenes.auth_new.data.CategoryWrapper
import com.limor.app.scenes.utils.BACKGROUND
import com.limor.app.scenes.utils.MAIN
import kotlinx.android.synthetic.main.fragment_new_auth_categories.*

class FragmentCategories : Fragment() {

    private val model: AuthViewModelNew by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_auth_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListeners()
        switchCommonVisibility(isLoading = true)
        subscribeToViewModel()
    }

    private fun switchCommonVisibility(isLoading: Boolean = false, hasError: Boolean = false) {
        val shouldShowError = !isLoading && hasError
        val errorVisibility = if (shouldShowError) View.VISIBLE else View.GONE
        tvErrorMessage.visibility = errorVisibility
        btnRetry.visibility = errorVisibility
        pbLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun subscribeToViewModel() {
        model.downloadCategories()
        model.categoriesLiveData.observe(viewLifecycleOwner, Observer {
            if (it.isEmpty())
                return@Observer
            switchCommonVisibility()
            createCategoriesArray(it)
        })

        model.categorySelectionDone.observe(viewLifecycleOwner, Observer {
            btnContinue.isEnabled = it
        })

        model.categoryLiveDataError.observe(viewLifecycleOwner, Observer {
            if (it.isNullOrEmpty()) return@Observer
            tvErrorMessage.text = it
            switchCommonVisibility(hasError = true)
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
        btnRetry.setOnClickListener {
            switchCommonVisibility(isLoading = true)
            model.downloadCategories()
        }

        btnContinue.setOnClickListener {
            it.findNavController()
                .navigate(R.id.action_fragment_new_auth_categories_to_fragment_new_auth_languages)
        }

        topAppBar.setNavigationOnClickListener {
            it.findNavController().popBackStack()
        }
    }
}