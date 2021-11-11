package com.limor.app.di.modules.fragments

import com.limor.app.scenes.patron.PatronPricingPlansFragment
import com.limor.app.scenes.patron.manage.fragment.*
import com.limor.app.scenes.patron.setup.FragmentPatronCategories
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ManagePatronActivityFragmentBuilderModule {

    @ContributesAndroidInjector
    abstract fun contributeManagePatronFragmentInjector(): ManagePatronFragment

    @ContributesAndroidInjector
    abstract fun contributeFragmentPatronCategories(): FragmentPatronCategories

    @ContributesAndroidInjector
    abstract fun contributeFragmentCastEarnings() : FragmentCastEarnings

    @ContributesAndroidInjector
    abstract fun contributeFragmentFragmentMyEarnings() : FragmentMyEarnings

    @ContributesAndroidInjector
    abstract fun contributeFragmentChangePrice(): FragmentChangePrice

    @ContributesAndroidInjector
    abstract fun contributeDialogConfirmationChangePrice(): DialogConfirmationChangePrice

}