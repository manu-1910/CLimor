package com.limor.app.scenes.main.fragments.setup_patron

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.limor.app.App
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.extensions.hideKeyboard
import com.limor.app.scenes.main.viewmodels.CategoriesViewModel
import com.limor.app.scenes.main.viewmodels.SetupPatronViewModel
import com.limor.app.scenes.utils.CommonsKt
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_setup_patron_select_category.*
import org.jetbrains.anko.padding
import javax.inject.Inject

class SetupPatronSelectCategoryFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var categoriesViewModel: CategoriesViewModel
    private lateinit var setupPatronViewModel: SetupPatronViewModel

    private var rootView: View? = null

    private val categoriesTrigger = PublishSubject.create<Unit>()
    var app: App? = null
    private var lastSelectedChipId: Int = 0
    private var lastSelectedChipName: String = ""

    companion object {
        val TAG: String = SetupPatronSelectCategoryFragment::class.java.simpleName
        fun newInstance() = SetupPatronSelectCategoryFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView =
                inflater.inflate(R.layout.fragment_setup_patron_select_category, container, false)
        }

        app = context?.applicationContext as App

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setup animation transition
        ViewCompat.setTranslationZ(view, 1f)

        listeners()
        bindViewModel()
        initApiCallGetCategories()
        setupToolbar()
    }

    override fun onStart() {
        super.onStart()
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBackPressed()
                }
            })
    }

    private fun onBackPressed() {
        setupPatronViewModel.categorySelectedId = lastSelectedChipId
        setupPatronViewModel.categorySelectedName = lastSelectedChipName
        findNavController().popBackStack()
    }


    private fun setupToolbar() {
        val tvToolbarTitle = activity?.findViewById<TextView>(R.id.tvToolbarTitle)
        tvToolbarTitle?.text = getString(R.string.title_category)
    }

    private fun initApiCallGetCategories() {

        val output = categoriesViewModel.transform(
            CategoriesViewModel.Input(
                categoriesTrigger
            )
        )

        output.response.observe(this, Observer {
            view?.hideKeyboard()
            if (it.code == 0) { //Tags Response Ok

                val params: LinearLayout.LayoutParams =
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                params.setMargins(16, 16, 16, 16)

                for (item in it.data.categories) {
                    val tvChip = TextView(context)
                    tvChip.id = item.id
                    tvChip.text = item.name
                    tvChip.setTextColor(
                        ContextCompat.getColorStateList(
                            context!!,
                            R.color.chip_textcolor
                        )
                    )
                    tvChip.isEnabled = true
                    tvChip.padding = 24

                    var colorBg = R.drawable.bg_chip_category
                    if(item.id == setupPatronViewModel.categorySelectedId)
                        colorBg = R.drawable.bg_chip_category_selected

                    tvChip.background = ContextCompat.getDrawable(
                        context!!,
                        colorBg
                    )
                    tvChip.layoutParams = params
                    tvChip.setOnClickListener {
                        selectChip(tvChip.id, tvChip.text.toString())
                    }
                    categoryChipsView?.addView(tvChip)
                }
            }
        })

        output.backgroundWorkingProgress.observe(this, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            view?.hideKeyboard()
            CommonsKt.handleOnApiError(app!!, context!!, this, it)
        })
    }

    override fun onResume() {
        super.onResume()
        categoriesTrigger.onNext(Unit)
    }


    private fun bindViewModel() {
        activity?.let {
            categoriesViewModel = ViewModelProviders
                .of(it, viewModelFactory)
                .get(CategoriesViewModel::class.java)

            setupPatronViewModel = ViewModelProviders
                .of(it, viewModelFactory)
                .get(SetupPatronViewModel::class.java)
            lastSelectedChipId = setupPatronViewModel.categorySelectedId
            lastSelectedChipName = setupPatronViewModel.categorySelectedName
        }
    }

    private fun selectChip(id: Int, name: String) {
        // let's unselect the last selected chip
        if (lastSelectedChipId > 0) {
            val lastSelectedChip = categoryChipsView?.findViewById<TextView>(lastSelectedChipId)
            if (lastSelectedChip != null) {
                lastSelectedChip.background = ContextCompat.getDrawable(
                    context!!,
                    R.drawable.bg_chip_category
                )
            }
        }
        lastSelectedChipId = id
        lastSelectedChipName = name

        // and now, let's select the current selected chip
        val currentSelectedChip = categoryChipsView?.findViewById<TextView>(id)
        if (currentSelectedChip != null) {
            currentSelectedChip.background = ContextCompat.getDrawable(
                context!!,
                R.drawable.bg_chip_category_selected
            )
        }
    }

    private fun listeners() {

    }


}