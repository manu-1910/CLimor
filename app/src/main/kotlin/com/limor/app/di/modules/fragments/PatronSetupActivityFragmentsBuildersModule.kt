package com.limor.app.di.modules.fragments

import com.limor.app.scenes.patron.FragmentPatronOnboardingSuccess
import com.limor.app.scenes.patron.PatronPricingPlansFragment
import com.limor.app.scenes.patron.setup.FragmentPatronCategories
import com.limor.app.scenes.patron.setup.FragmentPatronLanguages
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class PatronSetupActivityFragmentsBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeSetupPricingPlansFragmentInjector(): PatronPricingPlansFragment

    @ContributesAndroidInjector
    abstract fun contributeSetupPatronCategoriesInjector(): FragmentPatronCategories

    @ContributesAndroidInjector
    abstract fun contributeSetupPatronLanguagesInjector(): FragmentPatronLanguages

    @ContributesAndroidInjector
    abstract fun contributeSetupPatronOnBoardingSuccessInjector(): FragmentPatronOnboardingSuccess

   }