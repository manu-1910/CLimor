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
import org.jetbrains.anko.padding
import org.jetbrains.anko.sdk23.listeners.onClick
import javax.inject.Inject


class CategoriesFragment : BaseFragment() {

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


    companion object {
        val TAG: String = CategoriesFragment::class.java.simpleName
        fun newInstance() = CategoriesFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_record_categories, container, false)
        }
        chipGroup = rootView!!.findViewById(R.id.categoryChipsView)
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
            if (publishViewModel.categorySelected.isNotEmpty()) {
                findNavController().popBackStack()
            }
        }
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
        tvToolbarTitle?.text = getString(R.string.title_category)

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

                val params: LinearLayout.LayoutParams =
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                params.setMargins(16, 16, 16, 16)

                for (item in it.data.categories) {
                    val tvChip = Chip(context)
                    tvChip.id = item.id
                    tvChip.text = item.name
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
                        publishViewModel.categorySelected = tvChip.text.toString()
                        publishViewModel.categorySelectedId = tvChip.id
                    }
                    chipGroup!!.addView(tvChip)
                }
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

}

