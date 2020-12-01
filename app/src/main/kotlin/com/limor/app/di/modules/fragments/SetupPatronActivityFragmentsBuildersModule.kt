package com.limor.app.di.modules.fragments

import com.limor.app.scenes.main.fragments.setup_patron.SetupPatronFragment
import com.limor.app.scenes.main.fragments.setup_patron.SetupPatronPaymentFragment
import com.limor.app.scenes.main.fragments.setup_patron.SetupPatronSelectCategoryFragment
import com.limor.app.scenes.main.fragments.setup_patron.SetupPatronSettingsFragment
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
}