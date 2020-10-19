package io.square1.limor.scenes.main.fragments.record

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.ChipGroup
import io.reactivex.subjects.PublishSubject
import io.square1.limor.App
import io.square1.limor.R
import io.square1.limor.common.BaseFragment
import io.square1.limor.extensions.hideKeyboard
import io.square1.limor.scenes.authentication.SignActivity
import io.square1.limor.scenes.main.viewmodels.CategoriesViewModel
import io.square1.limor.scenes.main.viewmodels.PublishViewModel
import io.square1.limor.scenes.utils.CommonsKt
import io.square1.limor.uimodels.UICategory
import kotlinx.android.synthetic.main.toolbar_default.tvToolbarTitle
import kotlinx.android.synthetic.main.toolbar_with_back_arrow_icon.btnClose
import org.jetbrains.anko.okButton
import org.jetbrains.anko.padding
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.alert
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
            rootView = inflater.inflate(R.layout.fragment_categories, container, false)
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

        output.response.observe(this, Observer {
            view?.hideKeyboard()
            if (it.code == 0) { //Tags Response Ok

                val params: LinearLayout.LayoutParams =
                    LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(16, 16, 16, 16)

                val states = arrayOf(
                    intArrayOf(android.R.attr.state_enabled),
                    intArrayOf(-android.R.attr.state_enabled),
                    intArrayOf(-android.R.attr.state_checked),
                    intArrayOf(android.R.attr.state_pressed)
                )

                val colors = intArrayOf(
                    Color.BLACK,
                    Color.RED,
                    Color.GREEN,
                    Color.BLUE
                )

                for (item in it.data.categories) {
                    val tvChip = TextView(context)
                    tvChip.id = item.id
                    tvChip.text = item.name
                    tvChip.setTextColor(ContextCompat.getColorStateList(context!!, R.color.chip_textcolor))
                    tvChip.isEnabled = true
                    tvChip.padding = 24
                    tvChip.background = ContextCompat.getDrawable(
                        context!!,
                        R.drawable.bg_chip_category
                    )
                    tvChip.layoutParams = params
                    tvChip.setOnClickListener {
                        publishViewModel.categorySelected = tvChip.text.toString()
                        publishViewModel.categorySelectedId = tvChip.id
                        findNavController().popBackStack()
                    }
                    chipGroup!!.addView(tvChip)
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

}

