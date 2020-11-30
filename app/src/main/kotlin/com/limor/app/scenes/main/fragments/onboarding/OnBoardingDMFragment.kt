package com.limor.app.scenes.main.fragments.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.limor.app.R
import com.limor.app.common.BaseFragment
import kotlinx.android.synthetic.main.fragment_on_boarding_premium.*
import org.jetbrains.anko.sdk23.listeners.onClick

class OnBoardingDMFragment(private val skipPressedListener: OnBoardingFragment.OnSkipPressedListener) :
    BaseFragment() {

    private var rootView: View? = null

    companion object {
        val TAG: String = OnBoardingDMFragment::class.java.simpleName
        fun newInstance(skipPressedListener: OnBoardingFragment.OnSkipPressedListener) =
            OnBoardingDMFragment(skipPressedListener)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_on_boarding_dms, container, false)
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listeners()
    }

    private fun listeners() {
        btn_skip?.onClick {
            skipPressedListener.onSkipPressed()
        }
    }


}