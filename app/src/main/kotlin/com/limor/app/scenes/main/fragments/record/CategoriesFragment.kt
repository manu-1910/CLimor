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
import com.limor.app.uimodels.UISimpleCategory
import kotlinx.android.synthetic.main.fragment_publish_categories.*
import kotlinx.android.synthetic.main.view_follow_button.*
import org.jetbrains.anko.design.snackbar
import timber.log.Timber
import javax.inject.Inject


class CategoriesFragment : FragmentWithLoading(), Injectable {

    private val maxSelection: Int by lazy {
        if (arguments?.getBoolean("isPatron") == true) 5 else 1
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: PublishCategoriesViewModel by activityViewModels { viewModelFactory }
    private val publishViewModel: PublishViewModel by activityViewModels { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_publish_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListeners()
    }

    override fun load() = model.downloadCategories()

    var lastCheckedId = View.NO_ID
    var lastCheckedIds = hashSetOf<Int>()

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
        cgCategories.isSingleSelection = maxSelection==1
        if (categories.isNotEmpty()) cgCategories.removeAllViews()
        BACKGROUND({
            val categoriesChips =
                categories.map { category ->
                    getVariantChip(category)
                }
            MAIN {
                categoriesChips.forEach {
                    it.id = View.generateViewId()
                    cgCategories.addView(it)
                }
            }
        })
    }

    private fun getVariantChip(category: CategoryWrapper): Chip {
        val chip = layoutInflater.inflate(R.layout.item_chip_category, null) as Chip
        chip.text = category.name
        MAIN {
            chip.isChecked = publishViewModel.categorySelectedNamesList.any { it.name == category.name }
             Timber.d("Chip -> ${category.name} -- ${publishViewModel.categorySelectedNamesList}")
        }
        Timber.d("Chip -> ${category.categoryId} -- ${category.name}")

        chip.setOnCheckedChangeListener { buttonView, isChecked ->
            val ids: List<Int> = cgCategories.checkedChipIds
            if (isChecked) {
                //Get all checked chips in the group
                if (ids.size > maxSelection) {
                    chip.isChecked = false //force to unchecked the chip
                    chip.snackbar("You can only select $maxSelection categories")
                } else {
                    lastCheckedIds.add(chip.id)
                    category.categoryId?.let {
                        //publishViewModel.categorySelectedId = it
                        if (!publishViewModel.categorySelectedNamesList.any { cat -> cat.name == category.name }) {
                            publishViewModel.categorySelectedIdsList.add(it)
                            publishViewModel.categorySelectedNamesList.add(UISimpleCategory(category.name,
                                category.categoryId!!))
                            publishViewModel.categorySelected = getSelectedCategoriesText()
                        }
                    }
                }
                btnContinue.isEnabled =
                    cgCategories.checkedChipIds.isNotEmpty() && cgCategories.checkedChipIds.size <= maxSelection

            } else {
                lastCheckedId = View.NO_ID
                lastCheckedIds.remove(chip.id)
                publishViewModel.categorySelectedIdsList.remove(category.categoryId)
                publishViewModel.categorySelectedNamesList.removeIf { category.name == it.name }
                category.isSelected = false
                btnContinue.isEnabled =
                    cgCategories.checkedChipIds.isNotEmpty() && cgCategories.checkedChipIds.size <= maxSelection
                publishViewModel.categorySelected = getSelectedCategoriesText()
            }
        }

        return chip
    }

    fun getSelectedCategoriesText(): String {
        val selections = publishViewModel.categorySelectedNamesList
        return when {
            selections.isEmpty() -> {
                ""
            }
            cgCategories.checkedChipIds.size > 1 -> {
                "${selections[0].name} +${selections.size - 1}"
            }
            else -> {
                selections[0].name
            }
        }

    }

private fun setOnClickListeners() {
    btnContinue.setOnClickListener {
        //publishViewModel.categorySelectedNamesList.clear()
        findNavController().popBackStack()
    }

    topAppBar.setNavigationOnClickListener {
        findNavController().popBackStack()
    }
}
}

