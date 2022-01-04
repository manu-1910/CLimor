package com.limor.app.di.modules.fragments

import com.limor.app.scenes.auth_new.fragments.FragmentCountryCode
import com.limor.app.scenes.patron.unipaas.FragmentDigitalWalletSetUpConfirmation
import com.limor.app.scenes.patron.unipaas.FragmentSetUpDigitalWalletForm
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class UniPaasActivityFragmentBuilderModule {

    @ContributesAndroidInjector
    abstract fun contributeFragmentSetUpDigitalWalletForm(): FragmentSetUpDigitalWalletForm

    @ContributesAndroidInjector
    abstract fun contributeFragmentDigitalWalletSetUpConfirmation(): FragmentDigitalWalletSetUpConfirmation

    @ContributesAndroidInjector
    abstract fun contributeFragmentCountryCode(): FragmentCountryCode

}