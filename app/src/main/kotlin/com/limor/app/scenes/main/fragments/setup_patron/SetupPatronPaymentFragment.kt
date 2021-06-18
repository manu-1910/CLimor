package com.limor.app.scenes.main.fragments.setup_patron

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider

import androidx.navigation.fragment.findNavController
import com.limor.app.App
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.scenes.main.viewmodels.SetupPatronViewModel
import kotlinx.android.synthetic.main.fragment_setup_patron_payment.*
import org.jetbrains.anko.sdk23.listeners.onClick
import javax.inject.Inject

class SetupPatronPaymentFragment : BaseFragment() {

    enum class Currency {
        EURO, POUND, DOLLAR
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var setupPatronViewModel: SetupPatronViewModel

    private var rootView: View? = null

    private var app: App? = null

    private var currentSelectedCurrency = Currency.EURO

    companion object {
        val TAG: String = SetupPatronPaymentFragment::class.java.simpleName
        fun newInstance() = SetupPatronPaymentFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView =
                inflater.inflate(R.layout.fragment_setup_patron_payment, container, false)
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
        setupToolbar()
    }

    override fun onResume() {
        super.onResume()
        fillForm()
    }

    private fun fillForm() {
        swAllowDonations.isChecked = setupPatronViewModel.isAllowDonationsEnabled
        chkMonthly.isChecked = setupPatronViewModel.isMonthlyEnabled
        chkPerCreation.isChecked = setupPatronViewModel.isPerCreationEnabled
        showOrHidePricePerCast(chkPerCreation.isChecked)
        etCastPrice.setText(setupPatronViewModel.castPrice.toString())
        onCurrencyClicked(setupPatronViewModel.selectedCurrency)
    }


    private fun setupToolbar() {
        val tvToolbarTitle = activity?.findViewById<TextView>(R.id.tvToolbarTitle)
        tvToolbarTitle?.text = getString(R.string.title_payment)
        val btnInfo = activity?.findViewById<ImageButton>(R.id.btnInfo)
        btnInfo?.visibility = View.GONE
    }


    private fun bindViewModel() {
        activity?.let {
            setupPatronViewModel = ViewModelProvider(it, viewModelFactory)
                .get(SetupPatronViewModel::class.java)
        }
    }

    private fun listeners() {
        chkPerCreation?.setOnCheckedChangeListener { buttonView, isChecked ->
            run {
                showOrHidePricePerCast(isChecked)
            }
        }

        tvDollar.onClick {
            onCurrencyClicked(Currency.DOLLAR)
        }

        tvEuro.onClick {
            onCurrencyClicked(Currency.EURO)
        }

        tvPound.onClick {
            onCurrencyClicked(Currency.POUND)
        }

        btnSaveChanges?.onClick {
            if (validateForm()) {
                setupPatronViewModel.isAllowDonationsEnabled = swAllowDonations.isChecked
                setupPatronViewModel.isMonthlyEnabled = chkMonthly.isChecked
                setupPatronViewModel.isPerCreationEnabled = chkPerCreation.isChecked
                if (chkPerCreation.isChecked) {
                    try {
                        setupPatronViewModel.castPrice = etCastPrice.text.toString().toFloat()
                    } catch (e: Exception) {
                        setupPatronViewModel.castPrice = etCastPrice.text.toString().toFloat()
                    }
                } else {
                    setupPatronViewModel.castPrice = 0f
                }
                setupPatronViewModel.selectedCurrency = currentSelectedCurrency
                findNavController().popBackStack()
            }
        }
    }

    private fun showOrHidePricePerCast(show: Boolean) {
        if (show)
            layCastPrice.visibility = View.VISIBLE
        else
            layCastPrice.visibility = View.GONE
    }

    private fun validateForm(): Boolean {
        return true
    }

    private fun onCurrencyClicked(currency: Currency) {
        tvDollar.background = ContextCompat.getDrawable(context!!, R.drawable.bg_chip_category_unselected)
        tvPound.background = ContextCompat.getDrawable(context!!, R.drawable.bg_chip_category_unselected)
        tvEuro.background = ContextCompat.getDrawable(context!!, R.drawable.bg_chip_category_unselected)

        when (currency) {
            Currency.EURO -> tvEuro.background =
                ContextCompat.getDrawable(context!!, R.drawable.bg_chip_category_selected)
            Currency.POUND -> tvPound.background =
                ContextCompat.getDrawable(context!!, R.drawable.bg_chip_category_selected)
            Currency.DOLLAR -> tvDollar.background =
                ContextCompat.getDrawable(context!!, R.drawable.bg_chip_category_selected)
        }

        currentSelectedCurrency = currency
    }


}