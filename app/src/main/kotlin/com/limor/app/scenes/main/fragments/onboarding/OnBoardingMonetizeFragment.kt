package com.limor.app.scenes.main.fragments.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.scenes.main.fragments.setup_patron.SetupPatronActivity
import kotlinx.android.synthetic.main.fragment_on_boarding_monetize.*
import org.jetbrains.anko.sdk23.listeners.onClick

class OnBoardingMonetizeFragment : BaseFragment() {

    private var rootView: View? = null

    companion object {
        val TAG: String = OnBoardingMonetizeFragment::class.java.simpleName
        fun newInstance() = OnBoardingMonetizeFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_on_boarding_monetize, container, false)
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listeners()
    }

    private fun listeners() {
        btn_let_go?.onClick {
            val intent = Intent(activity, SetupPatronActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }
    }


}