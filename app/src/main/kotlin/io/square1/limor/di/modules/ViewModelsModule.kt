package io.square1.limor.di.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import io.square1.limor.common.ViewModelFactory
import io.square1.limor.di.ViewModelKey
import io.square1.limor.scenes.authentication.viewmodels.*
import io.square1.limor.scenes.main.viewmodels.*


@Module
abstract class ViewModelsModule {
    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(SignViewModel::class)
    abstract fun bindSignInViewModel(signViewModel: SignViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SignUpViewModel::class)
    abstract fun bindSignUpViewModel(signUpViewModel: SignUpViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ForgotPasswordViewModel::class)
    abstract fun bindForgotPasswordViewModel(forgotPasswordViewModel: ForgotPasswordViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SignFBViewModel::class)
    abstract fun bindSignFBViewModel(signFBViewModel: SignFBViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MergeFacebookAccountViewModel::class)
    abstract fun bindMergeFacebookAccountViewModel(mergeFacebookAccountViewModel: MergeFacebookAccountViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DraftViewModel::class)
    abstract fun bindDraftViewModel(draftViewModel: DraftViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileViewModel::class)
    abstract fun bindProfileViewModel(profileViewModel: ProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LogoutViewModel::class)
    abstract fun bindLogoutViewModel(logoutViewModel: LogoutViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PublishViewModel::class)
    abstract fun bindPublishViewModel(publishViewModel: PublishViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TagsViewModel::class)
    abstract fun bindTagsViewModel(tagsViewModel: TagsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LocationsViewModel::class)
    abstract fun bindLocationsViewModel(locationsViewModel: LocationsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FeedViewModel::class)
    abstract fun bindFeedViewModel(feedViewModel: FeedViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateFriendViewModel::class)
    abstract fun bindCreateFriendViewModel(createFriendViewModel: CreateFriendViewModel): ViewModel
}