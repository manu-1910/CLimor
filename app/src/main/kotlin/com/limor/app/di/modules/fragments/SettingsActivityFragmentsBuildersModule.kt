package com.limor.app.di.modules.fragments

import com.limor.app.scenes.auth_new.fragments.FragmentCountryCode
import com.limor.app.scenes.main.fragments.settings.*
import com.limor.app.scenes.main_new.fragments.FragmentDeleteAccountPhoneNumberInput
import com.limor.app.scenes.main_new.fragments.FragmentVerifyOtpForAccountDeletion
import dagger.Module
import dagger.android.ContributesAndroidInjector

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

    @ContributesAndroidInjector
    abstract fun contributeFragmentDeleteAccountPhoneNumberInputInjector(): FragmentDeleteAccountPhoneNumberInput

    @ContributesAndroidInjector
    abstract fun contributeFragmentCountryCode(): FragmentCountryCode

    @ContributesAndroidInjector
    abstract fun contributeFragmentVerifyOtpForAccountDeletion(): FragmentVerifyOtpForAccountDeletion

}