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
import kotlinx.android.synthetic.main.fragment_languages.*
import kotlinx.android.synthetic.main.fragment_patron_categories.*
import kotlinx.android.synthetic.main.fragment_publish_categories.*
import kotlinx.android.synthetic.main.fragment_publish_categories.btnContinue
import kotlinx.android.synthetic.main.fragment_publish_categories.topAppBar
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

    override fun showError() {
        errorTV.visibility = View.VISIBLE
    }

    override fun hideError() {
        errorTV.visibility = View.GONE
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

    private fun setOnClickListeners() {
        btnContinue.setOnClickListener {
            //update categories
            switchCommonVisibility(true)
            publishViewModel.addPatronCategories().observe(viewLifecycleOwner) {
                if (it == "Success") {
                    findNavController().navigate(R.id.action_fragmentPatronCategories_to_fragmentPatronLanguages)
                } else {
                    btnContinue.snackbar("Something went wrong")
                }
            }

        }


        topAppBar.setNavigationOnClickListener {
            activity?.finish()
        }
    }

}