package com.limor.app.scenes.main.fragments.setup_patron

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider

import com.limor.app.App
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.scenes.main.viewmodels.SetupPatronViewModel
import kotlinx.android.synthetic.main.fragment_setup_patron_settings.*
import org.jetbrains.anko.sdk23.listeners.onClick
import javax.inject.Inject


class SetupPatronSettingsFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var setupPatronViewModel: SetupPatronViewModel

    private var rootView: View? = null
    var app: App? = null

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
                inflater.inflate(R.layout.fragment_setup_patron_settings, container, false)
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
        fillFormFromViewModel()
    }

    private fun fillFormFromViewModel() {
        sw18Content.isChecked = setupPatronViewModel.plus18Activated
        swEarningsVisibility.isChecked = setupPatronViewModel.earningsVisibleActivated
        swPatronageVisibility.isChecked = setupPatronViewModel.patronageVisibleActivated
    }


    private fun setupToolbar() {
        val tvToolbarTitle = activity?.findViewById<TextView>(R.id.tvToolbarTitle)
        tvToolbarTitle?.text = getString(R.string.title_settings)
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
        btnSaveChanges?.onClick {
            setupPatronViewModel.plus18Activated = sw18Content.isChecked
            setupPatronViewModel.earningsVisibleActivated = swEarningsVisibility.isChecked
            setupPatronViewModel.patronageVisibleActivated = swPatronageVisibility.isChecked
            activity?.onBackPressed()
        }
    }


}