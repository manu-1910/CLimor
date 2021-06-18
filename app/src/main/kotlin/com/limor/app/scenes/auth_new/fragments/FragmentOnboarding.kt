package com.limor.app.scenes.auth_new.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.viewpager.widget.ViewPager
import com.limor.app.R
import com.limor.app.scenes.auth_new.AuthViewModelNew
import com.limor.app.scenes.auth_new.data.OnboardingInfo
import com.limor.app.scenes.auth_new.navigation.NavigationBreakpoints
import com.limor.app.scenes.auth_new.view.DepthPageTransformer
import kotlinx.android.synthetic.main.fragment_new_auth_onboarding.*
import timber.log.Timber

class FragmentOnboarding : Fragment() {
    private val model: AuthViewModelNew by activityViewModels()

    var currentPosition = 0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_auth_onboarding, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createAdapter()
        setOnClicks()
        subscribeToViewModel()
        saveNavigationBreakPoint(NavigationBreakpoints.ONBOARDING_COMPLETION.destination)
    }

    private fun subscribeToViewModel() {
        model.updateOnboardingStatusLiveData.observe(viewLifecycleOwner, Observer {
            it?.let {
                Timber.d("Update onboarding status liveData $it")
                navigateToHomeFeed()
            }
        })
    }

    private fun setOnClicks() {
        btnBack.setOnClickListener {
            model.updateUserOnboardingStatus(NavigationBreakpoints.HOME_FEED.destination)
        }

        btnContinue.setOnClickListener {
            vpOnboarding.setCurrentItem(currentPosition + 1, true)
        }
        btnFinish.setOnClickListener {
            model.updateUserOnboardingStatus(NavigationBreakpoints.HOME_FEED.destination)
        }
    }

    private fun navigateToHomeFeed() {
        view?.findNavController()?.navigate(R.id.go_to_main_activity)
        Timber.d("trying to finish activity")
        saveNavigationBreakPoint(NavigationBreakpoints.HOME_FEED.destination)
        requireActivity().finish()
    }

    private fun createAdapter() {
        vpOnboarding.setPageTransformer(true, DepthPageTransformer())
        vpOnboarding.adapter = ScreenSlidePagerAdapter(requireActivity().supportFragmentManager)
        tabDots.setupWithViewPager(vpOnboarding)
        vpOnboarding.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                currentPosition = position
                updateButtonsView()
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    private fun updateButtonsView() {
        when (currentPosition) {
            0, 1 -> {
                btnBack.visibility = View.VISIBLE
                btnContinue.visibility = View.VISIBLE
                btnFinish.visibility = View.GONE
            }
            2 -> {
                btnBack.visibility = View.GONE
                btnContinue.visibility = View.GONE
                btnFinish.visibility = View.VISIBLE
            }
        }
    }

    companion object {
        val onboardingInfoList = listOf(
            OnboardingInfo(
                R.drawable.onboarding_1,
                R.string.new_onboarding_title1,
                R.string.new_onboarding_subtitle1
            ),
            OnboardingInfo(
                R.drawable.onboarding_2,
                R.string.new_onboarding_title2,
                R.string.new_onboarding_subtitle2
            ),
            OnboardingInfo(
                R.drawable.onboarding_3,
                R.string.new_onboarding_title3,
                R.string.new_onboarding_subtitle3
            )
        )
    }

    private fun saveNavigationBreakPoint(destination: String?) {
        model.saveNavigationBreakPoint(requireContext(), destination)
    }
}

private class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
    override fun getCount(): Int = FragmentOnboarding.onboardingInfoList.size

    override fun getItem(position: Int): Fragment = FragmentOnboardingItem.newInstance(position)
}
