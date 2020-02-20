package io.square1.limor.di.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import io.square1.limor.common.ViewModelFactory
import io.square1.limor.di.ViewModelKey
import io.square1.limor.scenes.authentication.SignInSignUpViewModel


@Module
abstract class ViewModelsModule {
    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(SignInSignUpViewModel::class)
    abstract fun bindSignInSignUpViewModel(signInSignUpViewModel: SignInSignUpViewModel): ViewModel

}