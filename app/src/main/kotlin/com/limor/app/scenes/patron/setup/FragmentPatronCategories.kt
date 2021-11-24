package com.limor.app.scenes.patron.setup

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.limor.app.FragmentCategoriesSelectionBase
import com.limor.app.R
import com.limor.app.di.Injectable
import com.limor.app.scenes.auth_new.data.transform
import com.limor.app.scenes.main.viewmodels.PublishCategoriesViewModel
import com.limor.app.scenes.main.viewmodels.PublishViewModel
import com.limor.app.scenes.utils.BACKGROUND
import com.limor.app.scenes.utils.MAIN
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.uimodels.PatronCategoryUIModel
import com.skydoves.balloon.*
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_patron_categories.*
import kotlinx.android.synthetic.main.fragment_publish_categories.*
import kotlinx.android.synthetic.main.fragment_publish_categories.btnContinue
import kotlinx.android.synthetic.main.fragment_publish_categories.topAppBar
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class FragmentPatronCategories : FragmentCategoriesSelectionBase(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: PublishCategoriesViewModel by activityViewModels { viewModelFactory }
    private val publishViewModel: PublishViewModel by activityViewModels { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListeners()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    override fun load() = model.downloadCategories()

    override fun updateCategoriesSelection(id: Int, add: Boolean) {
        if(add){
            publishViewModel.categorySelectedIdsList.add(id)
        } else{
            publishViewModel.categorySelectedIdsList.remove(id)
        }
    }

    override val errorLiveData: LiveData<String>
        get() = model.categoryLiveDataError

    override fun subscribeToViewModel() {
        super.subscribeToViewModel()
        model.categoriesLiveData.observe(viewLifecycleOwner, Observer {
            if (it.isEmpty())
                return@Observer
            switchCommonVisibility()
            val list = mutableListOf<PatronCategoryUIModel>()
            it.forEach {
                 categoryWrapper -> list.add(categoryWrapper.transform())
            }
            createCategoriesArray(list)
        })

        model.categorySelectionDone.observe(viewLifecycleOwner, Observer {
            btnContinue.isEnabled = it
        })
    }

    /*private fun createCategoriesArray(categories: List<CategoryWrapper>) {
        cgCategories.isSingleSelection = false
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
    }*/

    /*private fun getVariantChip(category: CategoryWrapper): Chip {
        val chip = layoutInflater.inflate(R.layout.item_chip_category, null) as Chip
        chip.text = category.name
        MAIN {
            chip.isChecked = lastCheckedIds.contains(chip.id)
        }
        Timber.d("Chip -> ${category.categoryId} -- ${category.name}")
        chip.setOnCheckedChangeListener { buttonView, isChecked ->
            val ids: List<Int> = cgCategories.checkedChipIds
            Timber.d("$isChecked")
            if (isChecked) {
                category.isSelected = isChecked
                //Get all checked chips in the group
                if (ids.size > 5) {
                    chip.isChecked = false //force to unchecked the chip
                    chip.snackbar("You can only select 5 categories")
                } else {
                    lastCheckedIds.add(chip.id)
                    category.categoryId?.let {
                        //publishViewModel.categorySelectedId = it
                        publishViewModel.categorySelectedIdsList.add(it)
                    }
                }
            } else {
                lastCheckedId = View.NO_ID
                lastCheckedIds.remove(chip.id)
                chip.isChecked = false
                publishViewModel.categorySelectedIdsList.remove(category.categoryId)
                category.isSelected = false
                //model.updateCategoriesSelection()
            }
            btnContinue.isEnabled = ids.isNotEmpty()
        }
        return chip
    }*/

    private fun setOnClickListeners() {
        btnContinue.setOnClickListener {
            //update categories
            lifecycleScope.launch {
                switchCommonVisibility(true)
                publishViewModel.addPatronCategories().collect {
                    findNavController().navigate(R.id.action_fragmentPatronCategories_to_fragmentPatronLanguages)
                }
            }

        }
        val balloon = CommonsKt.createPopupBalloon(requireContext(),
            "You can only select 5 categories. if you talk about sport, Select `Sport`")
        btnCategoriesInfo.setOnClickListener {
            balloon.showAlignBottom(it)
            if (!balloon.isShowing) {
                it.showAlignBottom(balloon, 0, 0)
            } else {
                balloon.dismiss()
            }
        }

        topAppBar.setNavigationOnClickListener {
            activity?.finish()
        }
    }

}