package com.limor.app.scenes.main.fragments

import android.content.Intent
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
import com.limor.app.App
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.extensions.hideKeyboard
import com.limor.app.scenes.main.fragments.discover.DiscoverPodcastsByCategoryActivity
import com.limor.app.scenes.main.viewmodels.CategoriesViewModel
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.uimodels.UICategory
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_categories.*
import kotlinx.android.synthetic.main.toolbar_default.tvToolbarTitle
import kotlinx.android.synthetic.main.toolbar_with_back_arrow_icon.*
import org.jetbrains.anko.padding
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.toast
import javax.inject.Inject

class CategoriesFragment : BaseFragment() {

    private var rootView: View? = null
    var app: App? = null

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModelCategories: CategoriesViewModel

    private val getCategoriesDataTrigger = PublishSubject.create<Unit>()

    private var categories: ArrayList<UICategory> = ArrayList()


    companion object {
        val TAG: String = CategoriesFragment::class.java.simpleName
        fun newInstance() = CategoriesFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_categories, container, false)

            app = context?.applicationContext as App
        }
        return rootView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setup animation transition
        ViewCompat.setTranslationZ(view, 100f)

        bindViewModel()
        configureToolbar()
        hideTitle()
        initApiCallGetCategories()
        getCategoriesDataTrigger.onNext(Unit)
    }

    private fun configureToolbar() {
        //Toolbar title
        tvToolbarTitle?.text = getString(R.string.title_category)

        //Toolbar Left
        btnClose.onClick {
            activity?.finish()
        }
    }


    // this layout is reused, in this activity we don't want to show this titles
    private fun hideTitle() {
        layTitlesCategories.visibility = View.GONE
    }

    private fun bindViewModel() {
        activity?.let { fragmentActivity ->
            viewModelCategories = ViewModelProviders
                .of(fragmentActivity, viewModelFactory)
                .get(CategoriesViewModel::class.java)
        }
    }

    private fun initApiCallGetCategories() {
        val output = viewModelCategories.transform(
            CategoriesViewModel.Input(
                getCategoriesDataTrigger
            )
        )

        output.response.observe(this, Observer {
            if (it.code != 0) {
                toast(getString(R.string.couldnt_get_categories)).show()
            } else {
                categories = it.data.categories

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
                    tvChip.background = ContextCompat.getDrawable(
                        context!!,
                        R.drawable.bg_chip_category
                    )
                    tvChip.layoutParams = params
                    tvChip.setOnClickListener {
                        onCategoryClicked(item)
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

    private fun onCategoryClicked(item: UICategory) {
        val intent = Intent(requireActivity(), DiscoverPodcastsByCategoryActivity::class.java)
        intent.putExtra("category", item)
        startActivity(intent)
    }
}