package io.square1.limor.di.modules.fragments

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.square1.limor.scenes.authentication.SignInFragment

@Module
abstract class AuthenticationActivityFragmentsBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeAuthSignInSignUpFragmentInjector(): SignInFragment
}