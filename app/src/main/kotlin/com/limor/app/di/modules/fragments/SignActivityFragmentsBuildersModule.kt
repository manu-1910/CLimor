package com.limor.app.di.modules.fragments

import dagger.Module
import dagger.android.ContributesAndroidInjector
import com.limor.app.scenes.authentication.fragments.FacebookAuthFragment
import com.limor.app.scenes.authentication.fragments.ForgotPasswordFragment
import com.limor.app.scenes.authentication.fragments.SignInFragment
import com.limor.app.scenes.authentication.fragments.SignUpFragment

@Module
abstract class SignActivityFragmentsBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeSignInFragmentInjector(): SignInFragment

    @ContributesAndroidInjector
    abstract fun contributeSignUpFragmentInjector(): SignUpFragment

    @ContributesAndroidInjector
    abstract fun contributeForgotPasswordFragmentInjector(): ForgotPasswordFragment

    @ContributesAndroidInjector
    abstract fun contributeFacebookAuthFragmentInjector(): FacebookAuthFragment
}