package io.square1.limor.di.modules.fragments

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.square1.limor.scenes.authentication.fragments.SignInFragment
import io.square1.limor.scenes.authentication.fragments.SignUpFragment

@Module
abstract class SignActivityFragmentsBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeSignInFragmentInjector(): SignInFragment

    @ContributesAndroidInjector
    abstract fun contributeSignUpFragmentInjector(): SignUpFragment
}