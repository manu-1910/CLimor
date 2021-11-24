package com.limor.app.di.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.limor.app.common.ViewModelFactory
import com.limor.app.di.ViewModelKey
import com.limor.app.dm.SessionsViewModel
import com.limor.app.scenes.auth_new.AuthViewModelNew
import com.limor.app.scenes.authentication.viewmodels.*
import com.limor.app.scenes.main.fragments.profile.UserProfileViewModel
import com.limor.app.scenes.main.fragments.settings.SettingsViewModel
import com.limor.app.scenes.main.fragments.discover.category.DiscoverAllCategoriesViewModel
import com.limor.app.scenes.main.fragments.discover.category.DiscoverCategoryViewModel
import com.limor.app.scenes.main.fragments.discover.discover.DiscoverViewModel
import com.limor.app.scenes.main.fragments.discover.featuredcasts.DiscoverFeaturedCastsViewModel
import com.limor.app.scenes.main.fragments.discover.hashtag.DiscoverHashtagViewModel
import com.limor.app.scenes.main.fragments.discover.search.DiscoverSearchViewModel
import com.limor.app.scenes.main.fragments.discover.suggestedpeople.DiscoverSuggestedPeopleViewModel
import com.limor.app.scenes.main.fragments.profile.casts.UserPodcastsViewModel
import com.limor.app.scenes.main.viewmodels.*
import com.limor.app.scenes.main_new.view_model.HomeFeedViewModel
import com.limor.app.scenes.main_new.view_model.ListenPodcastViewModel
import com.limor.app.scenes.main_new.view_model.PodcastInteractionViewModel
import com.limor.app.scenes.main_new.view_model.UserMentionViewModel
import com.limor.app.scenes.notifications.NotificationViewModel
import com.limor.app.scenes.notifications.PushNotificationsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

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
    @ViewModelKey(LocationsViewModel::class)
    abstract fun bindLocationsViewModel(locationsViewModel: LocationsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GetUserViewModel::class)
    abstract fun getUserViewModel(getUserViewModel: GetUserViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PublishCategoriesViewModel::class)
    abstract fun bindPublishCatagoryViewModel(publishCatagoryViewModel: PublishCategoriesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LanguagesViewModel::class)
    abstract fun bindLanguagesViewModel(languagesViewModel: LanguagesViewModel) : ViewModel

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

    @Binds
    @IntoMap
    @ViewModelKey(SignUpFBViewModel::class)
    abstract fun bindSignUpFBViewModel(signUpFBViewModel: SignUpFBViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DeleteCommentViewModel::class)
    abstract fun bindDeleteCommentViewModel(deleteCommentViewModel: DeleteCommentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PushNotificationsViewModel::class)
    abstract fun bindPushNotificationsViewModel(pushNotificationsViewModel: PushNotificationsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateCommentDropOffViewModel::class)
    abstract fun bindCreateCommentDropOffViewModel(viewModel: CreateCommentDropOffViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreatePodcastDropOffViewModel::class)
    abstract fun bindCreatePodcastDropOffViewModel(viewModel: CreatePodcastDropOffViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SetupPatronViewModel::class)
    abstract fun bindSetupPatronViewModel(viewModel: SetupPatronViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GetUserFollowingsViewModel::class)
    abstract fun bindGetUserFollowingsViewModel(viewModel: GetUserFollowingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GetUserFollowersViewModel::class)
    abstract fun bindGetUserFollowersViewModel(viewModel: GetUserFollowersViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DiscoverAllCategoriesViewModel::class)
    abstract fun bindDiscoverAllCategoriesViewModel(viewModel: DiscoverAllCategoriesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DiscoverSuggestedPeopleViewModel::class)
    abstract fun bindDiscoverSuggestedPeopleViewModel(viewModel: DiscoverSuggestedPeopleViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DiscoverSearchViewModel::class)
    abstract fun bindDiscoverSearchViewModelViewModel(viewModel: DiscoverSearchViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DiscoverViewModel::class)
    abstract fun bindDiscoverViewModel(viewModel: DiscoverViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DiscoverCategoryViewModel::class)
    abstract fun bindDiscoverCategoryViewModel(viewModel: DiscoverCategoryViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DiscoverHashtagViewModel::class)
    abstract fun bindDiscoverHashtagViewModel(viewModel: DiscoverHashtagViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DiscoverFeaturedCastsViewModel::class)
    abstract fun bindDiscoverFeaturedCastsViewModel(viewModel: DiscoverFeaturedCastsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AuthViewModelNew::class)
    abstract fun bindAuthNewViewModel(viewModel: AuthViewModelNew): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HomeFeedViewModel::class)
    abstract fun bindHomeFeedNewViewModel(viewModel: HomeFeedViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    abstract fun bindSettingsViewModel(viewModel: SettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UserProfileViewModel::class)
    abstract fun bindFollowViewModel(viewModel: UserProfileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UserPodcastsViewModel::class)
    abstract fun bindUserPodcastsViewModel(viewModel: UserPodcastsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LikePodcastViewModel::class)
    abstract fun bindLikePodcastViewModel(viewModel: LikePodcastViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RecastPodcastViewModel::class)
    abstract fun bindRecastPodcastViewModel(viewModel: RecastPodcastViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CommentsViewModel::class)
    abstract fun bindGetCommentsForPodcastViewModel(viewModel: CommentsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HandleCommentActionsViewModel::class)
    abstract fun bindGetHandleCommentActionsViewModel(viewModel: HandleCommentActionsViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(PodcastViewModel::class)
    abstract fun bindPodcastViewModel(viewModel: PodcastViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SharePodcastViewModel::class)
    abstract fun bindSharePodcastViewModel(viewModel: SharePodcastViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PodcastInteractionViewModel::class)
    abstract fun bindPodcastInteractionViewModel(viewModel: PodcastInteractionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NotificationViewModel::class)
    abstract fun bindNotificationViewModel(viewModel: NotificationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ListenPodcastViewModel::class)
    abstract fun bindListenPodcastViewModel(viewModel: ListenPodcastViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UserMentionViewModel::class)
    abstract fun bindUserMentionViewModel(viewModel: UserMentionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SessionsViewModel::class)
    abstract fun bindSessionsViewModel(viewModel: SessionsViewModel): ViewModel
}
