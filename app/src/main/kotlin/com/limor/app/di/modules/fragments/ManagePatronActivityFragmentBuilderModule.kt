package com.limor.app.di.modules.fragments

import com.limor.app.scenes.patron.PatronPricingPlansFragment
import com.limor.app.scenes.patron.manage.fragment.ManagePatronFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ManagePatronActivityFragmentBuilderModule {
    @ContributesAndroidInjector
    abstract fun contributeManagePatronFragmentInjector(): ManagePatronFragment
}