package com.limor.app.scenes.patron.setup

import android.os.Bundle
import androidx.fragment.app.Fragment
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
import timber.log.Timber
import javax.inject.Inject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentPatronCategories.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentPatronCategories : FragmentWithLoading(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: PublishCategoriesViewModel by activityViewModels { viewModelFactory }
    private val publishViewModel: PublishViewModel by activityViewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_patron_categories, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) = FragmentPatronCategories()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListeners()
    }

    override fun load() = model.downloadCategories()

    var lastCheckedId = View.NO_ID

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
        cgCategories.isSingleSelection = true
        if (categories.isNotEmpty()) cgCategories.removeAllViews()
        BACKGROUND({
            val categoriesChips =
                categories.map { category ->
                    getVariantChip(category)
                }
            MAIN { categoriesChips.forEach {
                it.id = View.generateViewId()
                cgCategories.addView(it) } }
        })
    }

    private fun getVariantChip(category: CategoryWrapper): Chip {
        val chip = layoutInflater.inflate(R.layout.item_chip_category, null) as Chip
        chip.text = category.name
        MAIN {
            chip.isChecked = (lastCheckedId == chip.id)
        }
        Timber.d("Chip -> ${category.categoryId} -- ${category.name}")
        chip.setOnCheckedChangeListener { buttonView, isChecked ->

            if (isChecked) {
                category.isSelected = isChecked
                lastCheckedId = chip.id
                publishViewModel.categorySelected = chip.text.toString()
                category.categoryId?.let {
                    publishViewModel.categorySelectedId = it
                }
                model.updateCategoriesSelection()

            }else{
                lastCheckedId = View.NO_ID
                model.updateCategoriesSelection()
            }
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