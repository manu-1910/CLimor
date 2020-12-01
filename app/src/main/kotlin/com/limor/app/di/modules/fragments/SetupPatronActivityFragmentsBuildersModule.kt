package com.limor.app.di.modules.fragments

import com.limor.app.scenes.main.fragments.setup_patron.SetupPatronFragment
import com.limor.app.scenes.main.fragments.setup_patron.SetupPatronSelectCategoryFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SetupPatronActivityFragmentsBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeSetupPatronFragmentInjector(): SetupPatronFragment

    @ContributesAndroidInjector
    abstract fun contributeSetupPatronSelectCategoryFragmentInjector(): SetupPatronSelectCategoryFragment
}