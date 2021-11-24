package com.limor.app

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import com.google.android.material.chip.Chip
import com.limor.app.scenes.auth_new.fragments.FragmentWithLoading
import com.limor.app.scenes.utils.BACKGROUND
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.scenes.utils.MAIN
import com.limor.app.uimodels.PatronCategoryUIModel
import com.skydoves.balloon.showAlignBottom
import kotlinx.android.synthetic.main.fragment_patron_categories.*
import kotlinx.android.synthetic.main.fragment_publish_categories.*
import kotlinx.android.synthetic.main.fragment_publish_categories.btnContinue
import kotlinx.android.synthetic.main.fragment_publish_categories.cgCategories
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.support.v4.runOnUiThread
import timber.log.Timber

abstract class FragmentCategoriesSelectionBase : FragmentWithLoading() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    abstract override fun load()
    abstract fun updateCategoriesSelection(id: Int, add: Boolean)
    abstract override val errorLiveData: LiveData<String>

    var lastCheckedId = View.NO_ID
    var lastCheckedIds = hashSetOf<Int>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_patron_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setCommonClickListeners()
    }

    fun createCategoriesArray(categories: List<PatronCategoryUIModel?>) {
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
    }

    private fun getVariantChip(category: PatronCategoryUIModel?): Chip {
        val chip = layoutInflater.inflate(R.layout.item_chip_category, null) as Chip
        chip.text = category?.name
        MAIN {
            chip.isChecked = lastCheckedIds.contains(category?.id ?: chip.id)
        }
        Timber.d("Chip -> ${category?.id} -- ${category?.name}")
        chip.setOnCheckedChangeListener { buttonView, isChecked ->
            val ids: List<Int> = cgCategories.checkedChipIds
            Timber.d("$isChecked")
            if (isChecked) {
                category?.selected = isChecked
                //Get all checked chips in the group
                if (ids.size > 5) {
                    chip.isChecked = false //force to unchecked the chip
                    chip.snackbar("You can only select 5 categories")
                } else {
                    chip.id = category?.id!!
                    lastCheckedIds.add(chip.id)
                    category.id.let {
                        updateCategoriesSelection(it, true)
                    }
                }
            } else {
                lastCheckedId = View.NO_ID
                lastCheckedIds.remove(chip.id)
                chip.isChecked = false
                Log.d("Checked_Checked_unchecked", chip.id.toString())
                updateCategoriesSelection(category?.id ?: chip.id, false)
                category?.selected = false
            }
            btnContinue.isEnabled = ids.isNotEmpty()
        }
        runOnUiThread {
            chip.id = category?.id ?: -1
            chip.isChecked = category?.selected ?: false
            if(category?.selected == true){
                btnContinue.isEnabled = true
            }
            Log.d("Checked_Checked_chip", chip.id.toString() + "_" + chip.isChecked.toString())
        }
        return chip
    }

    private fun setCommonClickListeners() {
        val balloon = CommonsKt.createPopupBalloon(requireContext(),
            getString(R.string.category_selection_hint))
        btnCategoriesInfo.setOnClickListener {
            balloon.showAlignBottom(it)
            if (!balloon.isShowing) {
                it.showAlignBottom(balloon, 0, 0)
            } else {
                balloon.dismiss()
            }
        }
    }

}