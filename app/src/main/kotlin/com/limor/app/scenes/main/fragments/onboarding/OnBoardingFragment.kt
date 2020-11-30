package com.limor.app.scenes.main.fragments.onboarding

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.limor.app.R
import com.limor.app.common.BaseFragment
import kotlinx.android.synthetic.main.fragment_onboarding.*

class OnBoardingFragment : BaseFragment() {

    private var rootView: View? = null
    private lateinit var skipPressedListener: OnSkipPressedListener

    companion object {
        val TAG: String = OnBoardingFragment::class.java.simpleName
        fun newInstance() = OnBoardingFragment()
        private const val NUMBER_OF_VIEWPAGER_ITEMS = 3
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_onboarding, container, false)
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listeners()
        initViewPager()
    }

    private fun initViewPager() {

        val adapter = object :
            FragmentStatePagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getItem(position: Int): Fragment {
                return when (position) {
                    0 -> OnBoardingPremiumFragment.newInstance(skipPressedListener)
                    1 -> OnBoardingDMFragment.newInstance(skipPressedListener)
                    2 -> OnBoardingMonetizeFragment.newInstance()
                    else -> OnBoardingPremiumFragment.newInstance(skipPressedListener)
                }
            }

            override fun getCount(): Int {
                return NUMBER_OF_VIEWPAGER_ITEMS
            }

            override fun getPageTitle(position: Int): CharSequence {
                return ""
            }

            // this is necessary. Without this, app will crash when you are in a different fragment
            // and then push back and it goes back to this fragment.
            // the fragmentstatepageradapter saves states between different fragments of the adapter itself
            // but if you go to a different fragment, for example home, and the push back and the navigation
            // goes back to this profile fragment, the fragmentstatepageradapter will try to restore the
            // state of the adapter fragments but they are not alive anymore.
            override fun restoreState(state: Parcelable?, loader: ClassLoader?) {
                try {
                    super.restoreState(state, loader)
                } catch (e: Exception) {
//                        Timber.e("Error Restore State of Fragment : %s", e.message)
                }
            }
        }
        pagerOnBoarding?.adapter = adapter
        tabDots?.setupWithViewPager(pagerOnBoarding, true)

    }

    private fun listeners() {
        skipPressedListener = object : OnSkipPressedListener {
            override fun onSkipPressed() {
                pagerOnBoarding.currentItem = NUMBER_OF_VIEWPAGER_ITEMS - 1
            }
        }
    }

    interface OnSkipPressedListener {
        fun onSkipPressed()
    }


}