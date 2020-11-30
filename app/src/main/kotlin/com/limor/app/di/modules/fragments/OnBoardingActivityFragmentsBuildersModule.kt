package com.limor.app.di.modules.fragments

import com.limor.app.scenes.main.fragments.onboarding.OnBoardingDMFragment
import com.limor.app.scenes.main.fragments.onboarding.OnBoardingFragment
import com.limor.app.scenes.main.fragments.onboarding.OnBoardingMonetizeFragment
import com.limor.app.scenes.main.fragments.onboarding.OnBoardingPremiumFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class OnBoardingActivityFragmentsBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeOnBoardingFragmentInjector(): OnBoardingFragment

    @ContributesAndroidInjector
    abstract fun contributeOnBoardingPremiumFragmentInjector(): OnBoardingPremiumFragment

    @ContributesAndroidInjector
    abstract fun contributeOnBoardingDMFragmentInjector(): OnBoardingDMFragment

    @ContributesAndroidInjector
    abstract fun contributeOnBoardingMonetizeFragmentInjector(): OnBoardingMonetizeFragment
}