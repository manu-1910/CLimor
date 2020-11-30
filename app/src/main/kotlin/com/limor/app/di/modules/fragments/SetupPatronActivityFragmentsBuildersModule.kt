package com.limor.app.di.modules.fragments

import com.limor.app.scenes.main.fragments.setup_patron.SetupPatronFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SetupPatronActivityFragmentsBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeSetupPatronFragmentInjector(): SetupPatronFragment
}