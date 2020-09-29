package io.square1.limor.di.modules.fragments


import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.square1.limor.scenes.main.fragments.settings.ChangePasswordFragment
import io.square1.limor.scenes.main.fragments.settings.SettingsFragment


@Module
abstract class SettingsActivityFragmentsBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeSettingsFragmentInjector(): SettingsFragment

    @ContributesAndroidInjector
    abstract fun contributeChangePasswordFragmentInjector(): ChangePasswordFragment


}