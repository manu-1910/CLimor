package com.limor.app.scenes.patron.manage.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.limor.app.FragmentCategoriesSelectionBase
import com.limor.app.R
import com.limor.app.di.Injectable
import com.limor.app.scenes.auth_new.data.transform
import com.limor.app.scenes.patron.manage.viewmodels.ManagePatronViewModel
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.uimodels.PatronCategoryUIModel
import com.skydoves.balloon.showAlignBottom
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_patron_categories.*
import kotlinx.android.synthetic.main.fragment_publish_categories.btnContinue
import kotlinx.android.synthetic.main.fragment_publish_categories.topAppBar
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class FragmentUpdatePatronCategories : FragmentCategoriesSelectionBase(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: ManagePatronViewModel by activityViewModels { viewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    override fun load() {
        model.loadPatronCategories()
    }

    override fun updateCategoriesSelection(id: Int, add: Boolean) {
        if(add){
            model.categorySelectedIdsList.add(id)
        } else{
            model.categorySelectedIdsList.remove(id)
        }
    }

    override fun showError() {
        errorTV.visibility = View.VISIBLE
    }

    override fun hideError() {
        errorTV.visibility = View.GONE
    }

    override val errorLiveData: LiveData<String>
        get() = MutableLiveData()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        intialiseViews()
        setOnClickListeners()
        subscribeViewModels()
    }

    private fun intialiseViews(){
        btnContinue.text = getString(R.string.update_patron_categories)
    }

    private fun subscribeViewModels(){
        model.patronCategories.observe(viewLifecycleOwner, {
            if (it.isEmpty())
                return@observe
            switchCommonVisibility()
            createCategoriesArray(it)
        })
        model.categoryUpdateResult.observe(viewLifecycleOwner, {
            if(it == true){
                model.clearCategories()
                model.categorySelectedIdsList.clear()
                findNavController().navigateUp()
            }
        })
    }

    private fun setOnClickListeners() {
        btnContinue.setOnClickListener {
            lifecycleScope.launch {
                switchCommonVisibility(true)
                model.updatePatronCategories()
            }

        }

        topAppBar.setNavigationOnClickListener {
            model.clearCategories()
            model.categorySelectedIdsList.clear()
            findNavController().navigateUp()
        }
    }

}