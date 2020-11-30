package com.limor.app.scenes.main.fragments.setup_patron

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.scenes.main.viewmodels.SetupPatronViewModel
import kotlinx.android.synthetic.main.fragment_setup_patron.*
import kotlinx.android.synthetic.main.toolbar_with_back_arrow_icon.*
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.textColor
import javax.inject.Inject


class SetupPatronFragment : BaseFragment() {

    @Suppress("ClassName")
    private enum class FORM_PROGRESS {
        NOTHING, CATEGORY, TIERS, PAYMENT
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    private var currentProgress = 0

    private lateinit var setupPatronViewModel: SetupPatronViewModel

    private var rootView: View? = null

    companion object {
        val TAG: String = SetupPatronFragment::class.java.simpleName
        fun newInstance() = SetupPatronFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_setup_patron, container, false)
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //Setup animation transition
        ViewCompat.setTranslationZ(view, 1f)

        bindViewModels()
        listeners()
        setupToolbar()
    }

    private fun setupToolbar() {
        val tvToolbarTitle = activity?.findViewById<TextView>(R.id.tvToolbarTitle)
        tvToolbarTitle?.text = getString(R.string.limor_patron)
    }

    override fun onResume() {
        super.onResume()
        checkProgressStatus()
    }

    private fun checkProgressStatus() {
        if (setupPatronViewModel.categorySelectedId > 0) {
            setFormProgress(FORM_PROGRESS.CATEGORY)
        } else {
            setFormProgress(FORM_PROGRESS.NOTHING)
        }
    }

    private fun bindViewModels() {
        activity?.let {
            setupPatronViewModel = ViewModelProviders
                .of(it, viewModelFactory)
                .get(SetupPatronViewModel::class.java)
        }
    }

    private fun listeners() {
        dotCategory?.onClick {
            currentProgress++
            if (currentProgress > 2) {
                currentProgress = 0
            }
            val currentProgressEnum = when (currentProgress) {
                0 -> FORM_PROGRESS.CATEGORY
                1 -> FORM_PROGRESS.TIERS
                else -> FORM_PROGRESS.PAYMENT
            }
            setFormProgress(currentProgressEnum)
        }

        layCategory?.onClick {
            val navController = findNavController()
            navController.navigate(R.id.action_setup_patron_to_select_category)
        }

        layPayments?.onClick {
            toast("You clicked on paymets").show()
        }

        layTiers?.onClick {
            toast("You clicked on tiers").show()
        }

        laySettings?.onClick {
            toast("You clicked on settings").show()
        }

        btnCreatePatron?.onClick {
            toast("You clicked on CreatePatron").show()
        }
    }

    private fun setFormProgress(progress: FORM_PROGRESS) {
        when (progress) {
            FORM_PROGRESS.NOTHING -> {
                context?.let {
                    dotCategory.background =
                        ContextCompat.getDrawable(it, R.drawable.setup_patron_selector_default)
                    dotTiers.background =
                        ContextCompat.getDrawable(it, R.drawable.setup_patron_selector_default)
                    dotPayment.background =
                        ContextCompat.getDrawable(it, R.drawable.setup_patron_selector_default)
                    bar1.background = ContextCompat.getDrawable(it, R.color.brandSecondary200)
                    bar2.background = ContextCompat.getDrawable(it, R.color.brandSecondary200)
                    lblCategory.textColor = ContextCompat.getColor(it, R.color.brandSecondary200)
                    lblTiers.textColor = ContextCompat.getColor(it, R.color.brandSecondary200)
                    lblPayment.textColor = ContextCompat.getColor(it, R.color.brandSecondary200)
                }
            }
            FORM_PROGRESS.CATEGORY -> {
                context?.let {
                    dotCategory.background =
                        ContextCompat.getDrawable(it, R.drawable.setup_patron_selector_selected)
                    dotTiers.background =
                        ContextCompat.getDrawable(it, R.drawable.setup_patron_selector_default)
                    dotPayment.background =
                        ContextCompat.getDrawable(it, R.drawable.setup_patron_selector_default)
                    bar1.background = ContextCompat.getDrawable(it, R.color.brandSecondary200)
                    bar2.background = ContextCompat.getDrawable(it, R.color.brandSecondary200)
                    lblCategory.textColor = ContextCompat.getColor(it, R.color.white)
                    lblTiers.textColor = ContextCompat.getColor(it, R.color.brandSecondary200)
                    lblPayment.textColor = ContextCompat.getColor(it, R.color.brandSecondary200)
                }
            }
            FORM_PROGRESS.TIERS -> {
                context?.let {
                    dotCategory.background =
                        ContextCompat.getDrawable(it, R.drawable.setup_patron_selector_selected)
                    dotTiers.background =
                        ContextCompat.getDrawable(it, R.drawable.setup_patron_selector_selected)
                    dotPayment.background =
                        ContextCompat.getDrawable(it, R.drawable.setup_patron_selector_default)
                    bar1.background = ContextCompat.getDrawable(it, R.color.brandPrimary500)
                    bar2.background = ContextCompat.getDrawable(it, R.color.brandSecondary200)
                    lblCategory.textColor = ContextCompat.getColor(it, R.color.brandSecondary200)
                    lblTiers.textColor = ContextCompat.getColor(it, R.color.white)
                    lblPayment.textColor = ContextCompat.getColor(it, R.color.brandSecondary200)
                }
            }
            FORM_PROGRESS.PAYMENT -> {
                context?.let {
                    dotCategory.background =
                        ContextCompat.getDrawable(it, R.drawable.setup_patron_selector_selected)
                    dotTiers.background =
                        ContextCompat.getDrawable(it, R.drawable.setup_patron_selector_selected)
                    dotPayment.background =
                        ContextCompat.getDrawable(it, R.drawable.setup_patron_selector_selected)
                    bar1.background = ContextCompat.getDrawable(it, R.color.brandPrimary500)
                    bar2.background = ContextCompat.getDrawable(it, R.color.brandPrimary500)
                    lblCategory.textColor = ContextCompat.getColor(it, R.color.brandSecondary200)
                    lblTiers.textColor = ContextCompat.getColor(it, R.color.brandSecondary200)
                    lblPayment.textColor = ContextCompat.getColor(it, R.color.white)
                }
            }
        }
    }

}