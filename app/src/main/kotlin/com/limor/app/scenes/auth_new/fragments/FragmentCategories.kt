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
import com.limor.app.scenes.auth_new.data.Category
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
        subscribeToViewModel()
    }

    private fun subscribeToViewModel() {
        model.downloadCategories()
        model.categoriesLiveData.observe(viewLifecycleOwner, Observer {
            createCategoriesArray(it)
        })

        model.categorySelectionDone.observe(viewLifecycleOwner, Observer {
            btnContinue.isEnabled = it
        })
    }

    private fun createCategoriesArray(categories:List<Category>) {
        if (categories.isNotEmpty()) cgCategories.removeAllViews()
        categories.forEach { category ->
            val chip = getVariantChip(category)
            cgCategories.addView(chip)
        }
    }

    private fun getVariantChip(category: Category): Chip {
        val chip = layoutInflater.inflate(R.layout.item_chip_category, null) as Chip
        chip.text = category.name
        chip.isChecked = category.isSelected
        chip.setOnCheckedChangeListener { buttonView, isChecked ->
            category.isSelected = isChecked
            model.updateCategoriesSelection()
        }
        return chip
    }

    private fun setOnClickListeners() {
        btnContinue.setOnClickListener {
//            it.findNavController()
//                .navigate(R.id.)
        }

        topAppBar.setNavigationOnClickListener {
           it.findNavController().popBackStack()
        }
    }
}