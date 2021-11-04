package com.limor.app.di.modules.fragments

import com.limor.app.scenes.patron.PatronPricingPlansFragment
import com.limor.app.scenes.patron.manage.fragment.ManagePatronFragment
import com.limor.app.scenes.patron.setup.FragmentPatronCategories
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ManagePatronActivityFragmentBuilderModule {
    @ContributesAndroidInjector
    abstract fun contributeManagePatronFragmentInjector(): ManagePatronFragment
    @ContributesAndroidInjector
    abstract fun contributeFragmentPatronCategories(): FragmentPatronCategories
}