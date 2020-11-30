package com.limor.app.scenes.main.fragments.setup_patron

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.limor.app.R
import com.limor.app.common.BaseFragment
import kotlinx.android.synthetic.main.fragment_setup_patron.*
import org.jetbrains.anko.sdk23.listeners.onClick
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.textColor

class SetupPatronFragment : BaseFragment() {

    @Suppress("ClassName")
    private enum class FORM_PROGRESS {
        CATEGORY, TIERS, PAYMENT
    }

    private var currentProgress = 0

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
        listeners()
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
            toast("You clicked on category").show()
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