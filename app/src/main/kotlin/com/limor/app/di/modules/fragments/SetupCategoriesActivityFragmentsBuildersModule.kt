package com.limor.app.di.modules.fragments

import com.limor.app.scenes.main.fragments.CategoriesFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SetupCategoriesActivityFragmentsBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeCategoriesFragmentInjector(): CategoriesFragment
}

