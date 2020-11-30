package com.limor.app.scenes.main.fragments.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.limor.app.R
import com.limor.app.common.BaseFragment
import com.limor.app.scenes.main.fragments.onboarding.OnBoardingActivity
import kotlinx.android.synthetic.main.fragment_join_to_limor.*
import org.jetbrains.anko.sdk23.listeners.onClick


class JoinToPatronFragment : BaseFragment() {

    private var rootView: View? = null

    companion object {
        val TAG: String = JoinToPatronFragment::class.java.simpleName
        fun newInstance() = JoinToPatronFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_join_to_limor, container, false)
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listeners()
    }

    private fun listeners() {
        patronBanner.onClick {
            val intent = Intent(activity, OnBoardingActivity::class.java)
            startActivity(intent)
        }
    }


}