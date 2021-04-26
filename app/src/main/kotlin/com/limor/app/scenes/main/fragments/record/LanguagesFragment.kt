package com.limor.app.scenes.main.fragments.record


import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.limor.app.App
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.extensions.hideKeyboard
import com.limor.app.extensions.px
import com.limor.app.scenes.main.viewmodels.CategoriesViewModel
import com.limor.app.scenes.main.viewmodels.PublishViewModel
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.uimodels.UICategory
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_record_categories.*
import kotlinx.android.synthetic.main.toolbar_default.tvToolbarTitle
import kotlinx.android.synthetic.main.toolbar_with_back_arrow_icon.btnClose
import kotlinx.android.synthetic.main.toolbar_with_searchview.*
import org.jetbrains.anko.padding
import org.jetbrains.anko.sdk23.listeners.onClick
import javax.inject.Inject


class LanguagesFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var categoriesViewModel: CategoriesViewModel
    private lateinit var publishViewModel: PublishViewModel

    private var rootView: View? = null
    private var listCategories = ArrayList<UICategory>()
    private var listCategoriesSelected = ArrayList<UICategory>()
    private val categoriesTrigger = PublishSubject.create<Unit>()
    private var chipGroup: ChipGroup? = null
    var app: App? = null

    val mockLanguages =
        listOf(1 to "English", 2 to "Arabic", 3 to "Urdu", 4 to "Spanish", 5 to "Irish")
    var languages = mockLanguages

    companion object {
        val TAG: String = LanguagesFragment::class.java.simpleName
        fun newInstance() = LanguagesFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_languages, container, false)
        }
        chipGroup = rootView!!.findViewById(R.id.languagesChipsView)
        app = context?.applicationContext as App
        return rootView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setup animation transition
        ViewCompat.setTranslationZ(view, 100f)

        bindViewModel()
        configureToolbar()
        apiCallGetCategories()

        btnContinue.onClick {
            if (publishViewModel.languageSelected.isNotEmpty()) {
                findNavController().popBackStack()
            }
        }

        search_view.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                searchLanguages(newText)
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                searchLanguages(query)
                return false
            }
        })

    }

    private fun searchLanguages(text: String) {
        languages = if (text.isNotEmpty()) {
            mockLanguages.filter { it.second.contains(text, true) }
        } else mockLanguages
        chipGroup?.removeAllViews()
        setupLanguges()
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

            publishViewModel = ViewModelProviders
                .of(it, viewModelFactory)
                .get(PublishViewModel::class.java)
        }
    }


    private fun configureToolbar() {
        //Toolbar title
        tvToolbarTitle?.text = getString(R.string.title_languages)

        //Toolbar Left
        btnClose.onClick {
            categoriesViewModel.localListCategoriesSelected.clear()
            categoriesViewModel.localListCategories.clear()

            categoriesViewModel.localListCategories.addAll(listCategories)
            categoriesViewModel.localListCategoriesSelected.addAll(listCategoriesSelected)
            findNavController().popBackStack()
        }
    }


    private fun apiCallGetCategories() {
        val output = categoriesViewModel.transform(
            CategoriesViewModel.Input(
                categoriesTrigger
            )
        )

        output.response.observe(viewLifecycleOwner, Observer {
            view?.hideKeyboard()
            if (it.code == 0) { //Tags Response Ok
                setupLanguges()
            }
        })

        output.backgroundWorkingProgress.observe(viewLifecycleOwner, Observer {
            trackBackgroudProgress(it)
        })

        output.errorMessage.observe(this, Observer {
            view?.hideKeyboard()
            CommonsKt.handleOnApiError(app!!, requireContext(), this, it)
        })
    }

    private fun setupLanguges() {
        val params: LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        params.setMargins(16, 16, 16, 16)

        for (item in languages) {
            val tvChip = Chip(context)
            tvChip.id = item.first
            tvChip.text = item.second
            tvChip.isEnabled = true
            tvChip.padding = 24
            tvChip.isCheckable = true
            tvChip.setTextAppearance(requireContext(), R.style.ChipTextStyle)
            tvChip.isClickable = true
            tvChip.isFocusable = true
            tvChip.checkedIcon = null
            tvChip.gravity = Gravity.CENTER

            val shapeAppearanceModel = ShapeAppearanceModel()
                .toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, 4.px.toFloat())
                .build()
            tvChip.shapeAppearanceModel = shapeAppearanceModel
            tvChip.chipBackgroundColor = ContextCompat.getColorStateList(
                requireContext(),
                R.color.chip_backgroundcolor
            )
            tvChip.setOnClickListener {
                publishViewModel.languageSelected = tvChip.text.toString()
                publishViewModel.languageSelectedId = tvChip.id
            }
            chipGroup!!.addView(tvChip)
        }
    }
}

