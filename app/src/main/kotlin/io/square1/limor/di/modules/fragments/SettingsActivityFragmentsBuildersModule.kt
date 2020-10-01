package io.square1.limor.di.modules.fragments


import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.square1.limor.scenes.main.fragments.settings.*


@Module
abstract class SettingsActivityFragmentsBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeSettingsFragmentInjector(): SettingsFragment

    @ContributesAndroidInjector
    abstract fun contributeChangePasswordFragmentInjector(): ChangePasswordFragment

    @ContributesAndroidInjector
    abstract fun contributeWebViewFragmentInjector(): WebViewFragment

    @ContributesAndroidInjector
    abstract fun contributeEditProfileFragmentInjector(): EditProfileFragment

    @ContributesAndroidInjector
    abstract fun contributeBlockedUsersFragmentInjector(): BlockedUsersFragment
}