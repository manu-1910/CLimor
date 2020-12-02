package com.limor.app.di.modules.fragments

import com.limor.app.scenes.main.fragments.setup_patron.*
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SetupPatronActivityFragmentsBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeSetupPatronFragmentInjector(): SetupPatronFragment

    @ContributesAndroidInjector
    abstract fun contributeSetupPatronSelectCategoryFragmentInjector(): SetupPatronSelectCategoryFragment

    @ContributesAndroidInjector
    abstract fun contributeSetupPatronSettingsFragmentInjector(): SetupPatronSettingsFragment

    @ContributesAndroidInjector
    abstract fun contributeSetupPatronPaymentFragmentInjector(): SetupPatronPaymentFragment

    @ContributesAndroidInjector
    abstract fun contributeSetupPatronTiersFragmentInjector(): SetupPatronTiersFragment

    @ContributesAndroidInjector
    abstract fun contributeSetupPatronNewTierFragmentInjector(): SetupPatronNewTierFragment
}