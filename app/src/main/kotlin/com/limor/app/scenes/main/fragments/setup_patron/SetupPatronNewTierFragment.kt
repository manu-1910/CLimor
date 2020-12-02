package com.limor.app.scenes.main.fragments.setup_patron

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.limor.app.App
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.scenes.main.viewmodels.SetupPatronViewModel
import kotlinx.android.synthetic.main.fragment_setup_patron_new_tier.*
import org.jetbrains.anko.sdk23.listeners.onClick
import javax.inject.Inject


class SetupPatronNewTierFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var setupPatronViewModel: SetupPatronViewModel

    private var rootView: View? = null
    var app: App? = null

    private lateinit var tier: SetupPatronTiersFragment.Tier

    companion object {
        val TAG: String = SetupPatronSettingsFragment::class.java.simpleName
        fun newInstance() = SetupPatronSettingsFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView =
                inflater.inflate(R.layout.fragment_setup_patron_new_tier, container, false)
        }

        app = context?.applicationContext as App

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setup animation transition
        ViewCompat.setTranslationZ(view, 1f)

        setupToolbar()
        listeners()
        bindViewModel()
    }

    override fun onResume() {
        super.onResume()
        fillFormFromViewModel()
    }

    private fun fillFormFromViewModel() {
        etTierPrice.setText(setupPatronViewModel.currentModifyingTier?.price.toString())
        etTierBenefits.setText(setupPatronViewModel.currentModifyingTier?.benefits)
        etTierName.setText(setupPatronViewModel.currentModifyingTier?.name)
    }

    private fun listeners() {
        btnSaveChanges?.onClick {
            setupPatronViewModel.currentModifyingTier?.price = etTierPrice.text.toString().toFloat()
            setupPatronViewModel.currentModifyingTier?.benefits = etTierBenefits.text.toString()
            setupPatronViewModel.currentModifyingTier?.name = etTierName.text.toString()
            findNavController().popBackStack()
        }
    }


    private fun setupToolbar() {
        val tvToolbarTitle = activity?.findViewById<TextView>(R.id.tvToolbarTitle)
        tvToolbarTitle?.text = getString(R.string.tier_details)
        val btnInfo = activity?.findViewById<ImageButton>(R.id.btnInfo)!!
        btnInfo.visibility = View.GONE
    }


    private fun bindViewModel() {
        activity?.let {
            setupPatronViewModel = ViewModelProviders
                .of(it, viewModelFactory)
                .get(SetupPatronViewModel::class.java)
        }
    }

}