package com.limor.app.scenes.auth_new.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.limor.app.R
import kotlinx.android.synthetic.main.fragment_new_auth_onboarding_item.*

class FragmentOnboardingItem : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_auth_onboarding_item, container, false)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val page = arguments?.getInt(POSITION_ARGUMENT, 0) ?: 0
        val info = FragmentOnboarding.onboardingInfoList[page]
        ivOnboardingItem.setImageDrawable(resources.getDrawable( info.image))
        tvTitleOnboarding.setText(info.title)
        tvSubtitleOnboarding.setText(info.subTitle)
    }

    companion object{
        const val POSITION_ARGUMENT = "position"
        fun newInstance(position: Int): FragmentOnboardingItem {
            val instance = FragmentOnboardingItem()
            val args = Bundle()
            args.putInt(POSITION_ARGUMENT, position)
            instance.arguments = args
            return instance
        }
    }
}