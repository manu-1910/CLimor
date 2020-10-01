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
    @ViewModelKey(LogoutViewModel::class)
    abstract fun bindLogoutViewModel(logoutViewModel: LogoutViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PublishViewModel::class)
    abstract fun bindPublishViewModel(publishViewModel: PublishViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CategoriesViewModel::class)
    abstract fun bindCategoriesViewModel(categoriesViewModel: CategoriesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LocationsViewModel::class)
    abstract fun bindLocationsViewModel(locationsViewModel: LocationsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GetUserViewModel::class)
    abstract fun getUserViewModel(getUserViewModel: GetUserViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FeedViewModel::class)
    abstract fun bindFeedViewModel(feedViewModel: FeedViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FeedByTagViewModel::class)
    abstract fun bindFeedByTagViewModel(feedByTagViewModel: FeedByTagViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GetPodcastsByUserIDViewModel::class)
    abstract fun bindGetPodcastsByUserIDViewModel(getPodcastsByUserIDViewModel: GetPodcastsByUserIDViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateFriendViewModel::class)
    abstract fun bindCreateFriendViewModel(createFriendViewModel: CreateFriendViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DeleteFriendViewModel::class)
    abstract fun bindDeleteFriendViewModel(deleteFriendViewModel: DeleteFriendViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreatePodcastLikeViewModel::class)
    abstract fun bindCreatePodcastLikeViewModel(createPodcastLikeViewModel: CreatePodcastLikeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DeletePodcastLikeViewModel::class)
    abstract fun bindDeletePodcastLikeViewModel(deletePodcastLikeViewModel: DeletePodcastLikeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreatePodcastRecastViewModel::class)
    abstract fun bindCreatePodcastRecastViewModel(createPodcastRecastViewModel: CreatePodcastRecastViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DeletePodcastRecastViewModel::class)
    abstract fun bindDeletePodcastRecastViewModel(deletePodcastRecastViewModel: DeletePodcastRecastViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateBlockedUserViewModel::class)
    abstract fun bindCreateBlockedUserViewModel(createBlockedUserViewModel: CreateBlockedUserViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DeleteBlockedUserViewModel::class)
    abstract fun bindDeleteBlockedUserViewModel(deleteBlockedUserViewModel: DeleteBlockedUserViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateCommentLikeViewModel::class)
    abstract fun bindCreateCommentLikeViewModel(createCommentLikeViewModel: CreateCommentLikeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DeleteCommentLikeViewModel::class)
    abstract fun bindDeleteCommentLikeViewModel(deleteCommentLikeViewModel: DeleteCommentLikeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GetPodcastCommentsViewModel::class)
    abstract fun bindGetPodcastCommentsViewModel(getPodcastCommentsViewModel: GetPodcastCommentsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GetCommentCommentsViewModel::class)
    abstract fun bindGetCommentCommentsViewModel(getCommentCommentsViewModel: GetCommentCommentsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateCommentCommentViewModel::class)
    abstract fun bindCreateCommentCommentsViewModel(createCommentCommentsViewModel: CreateCommentCommentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreatePodcastCommentViewModel::class)
    abstract fun bindCreatePodcastCommentsViewModel(createPodcastCommentsViewModel: CreatePodcastCommentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TagsViewModel::class)
    abstract fun bindTagsViewModel(tagsViewModel: TagsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateCommentReportViewModel::class)
    abstract fun bindCreateCommentReportViewModel(createCommentReportViewModel: CreateCommentReportViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreatePodcastReportViewModel::class)
    abstract fun bindCreatePodcastReportViewModel(createPodcastReportViewModel: CreatePodcastReportViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateUserReportViewModel::class)
    abstract fun bindCreateUserReportViewModel(createUserReportViewModel: CreateUserReportViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NotificationsViewModel::class)
    abstract fun bindNotificationsViewModel(notificationsViewModel: NotificationsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GetUserLikedPodcastsViewModel::class)
    abstract fun bindGetUserLikedPodcastsViewModel(getUserLikedPodcastsViewModel: GetUserLikedPodcastsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DiscoverViewModel::class)
    abstract fun bindDiscoverViewModel(discoverViewModel: DiscoverViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DiscoverAccountsViewModel::class)
    abstract fun bindDiscoverAccountsViewModel(discoverAccountsViewModel: DiscoverAccountsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DiscoverHashTagsViewModel::class)
    abstract fun bindDiscoverHashTagsViewModel(discoverHashTagsViewModel: DiscoverHashTagsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChangePasswordViewModel::class)
    abstract fun bindChangePasswordViewModel(changePasswordViewModel: ChangePasswordViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GetBlockedUsersViewModel::class)
    abstract fun bindGetBlockedUsersViewModel(getBlockedUsersViewModel: GetBlockedUsersViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UpdateUserViewModel::class)
    abstract fun bindUpdateUserViewModel(updateUserViewModel: UpdateUserViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DeletePodcastViewModel::class)
    abstract fun bindDeletePodcastViewModel(deletePodcastViewModel: DeletePodcastViewModel): ViewModel
}