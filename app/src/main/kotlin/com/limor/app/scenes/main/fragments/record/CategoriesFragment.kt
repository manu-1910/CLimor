package com.limor.app.scenes.main.fragments.record


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.limor.app.R
import com.limor.app.di.Injectable
import com.limor.app.scenes.auth_new.data.CategoryWrapper
import com.limor.app.scenes.auth_new.fragments.FragmentWithLoading
import com.limor.app.scenes.main.viewmodels.PublishCategoriesViewModel
import com.limor.app.scenes.main.viewmodels.PublishViewModel
import com.limor.app.scenes.utils.BACKGROUND
import com.limor.app.scenes.utils.MAIN
import kotlinx.android.synthetic.main.fragment_publish_categories.*
import javax.inject.Inject


class CategoriesFragment : FragmentWithLoading(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: PublishCategoriesViewModel by activityViewModels {viewModelFactory}
    private val publishViewModel: PublishViewModel by activityViewModels {viewModelFactory}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_publish_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListeners()
    }

    override fun load() = model.downloadCategories()

    override val errorLiveData: LiveData<String>
        get() = model.categoryLiveDataError

    override fun subscribeToViewModel() {
        super.subscribeToViewModel()
        model.categoriesLiveData.observe(viewLifecycleOwner, Observer {
            if (it.isEmpty())
                return@Observer
            switchCommonVisibility()
            createCategoriesArray(it)
        })

        model.categorySelectionDone.observe(viewLifecycleOwner, Observer {
            btnContinue.isEnabled = it
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
            if (isChecked) publishViewModel.categorySelected = chip.text.toString()
        }
        return chip
    }

    private fun setOnClickListeners() {
        btnContinue.setOnClickListener {
            findNavController().popBackStack()
        }

        topAppBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }
}

