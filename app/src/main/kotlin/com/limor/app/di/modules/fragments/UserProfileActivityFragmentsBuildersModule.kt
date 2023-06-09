package com.limor.app.di.modules.fragments

import com.limor.app.scenes.main.fragments.profile.FragmentPurchases
import com.limor.app.dm.ui.ShareFragment
import com.limor.app.playlists.PlaylistsFragment
import com.limor.app.playlists.SaveToPlaylistFragment
import com.limor.app.scenes.main.fragments.ProfileFragment
import com.limor.app.scenes.main.fragments.discover.hashtag.DiscoverHashtagFragment
import com.limor.app.scenes.main.fragments.profile.*
import com.limor.app.scenes.main.fragments.profile.casts.UserPodcastsFragmentNew
import com.limor.app.scenes.main_new.fragments.DialogPodcastMoreActions
import com.limor.app.scenes.main_new.fragments.DialogPodcastReportP2
import com.limor.app.scenes.main_new.fragments.ExtendedPlayerFragment
import com.limor.app.scenes.main_new.fragments.SmallPlayerFragment
import com.limor.app.scenes.main_new.fragments.comments.FragmentCommentReplies
import com.limor.app.scenes.main_new.fragments.comments.FragmentComments
import com.limor.app.scenes.main_new.view.editpreview.EditPreviewFragment
import com.limor.app.scenes.patron.FragmentPlaylistDetails
import com.limor.app.scenes.profile.DialogCommentMoreActions
import com.limor.app.scenes.profile.DialogUserProfileActions
import com.limor.app.scenes.profile.DialogUserReport
import com.limor.app.scenes.utils.FragmentCreatePlaylist
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class UserProfileActivityFragmentsBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeUserProfileFragmentInjector(): UserProfileFragment

    @ContributesAndroidInjector
    abstract fun contributeUserPatronFragmentInjector(): UserPatronFragmentNew

    @ContributesAndroidInjector
    abstract fun contributeProfileFragmentInjector(): ProfileFragment

    @ContributesAndroidInjector
    abstract fun contributeJoinToPatronFragmentInjector(): JoinToPatronFragment

    @ContributesAndroidInjector
    abstract fun contributeUserFollowersFragmentInjector(): UserFollowersFragment

    @ContributesAndroidInjector
    abstract fun contributeUserFollowingsFragmentInjector(): UserFollowingsFragment

    @ContributesAndroidInjector
    abstract fun contributeUserPodcastsFragmentNewInjector(): UserPodcastsFragmentNew

    @ContributesAndroidInjector
    abstract fun contributeDialogUserProfileActionsFragmentInjector(): DialogUserProfileActions

    @ContributesAndroidInjector
    abstract fun contributeDialogUserCastsActionsFragmentInjector(): DialogPodcastMoreActions

    @ContributesAndroidInjector
    abstract fun contributeUserReportFragment(): DialogUserReport

    @ContributesAndroidInjector
    abstract fun contributePodcastReportFragment(): DialogPodcastReportP2

    @ContributesAndroidInjector
    abstract fun contributeExtendedPlayerFragmentInjector(): ExtendedPlayerFragment

    @ContributesAndroidInjector
    abstract fun contributeSmallPlayerFragmentInjector(): SmallPlayerFragment

    @ContributesAndroidInjector
    abstract fun contributeCommentsFragmentInjector(): FragmentComments

    @ContributesAndroidInjector
    abstract fun contributeFragmentCommentRepliesInjector(): FragmentCommentReplies

    @ContributesAndroidInjector
    abstract fun contributeDiscoverHashtagFragmentInjector(): DiscoverHashtagFragment

    @ContributesAndroidInjector
    abstract fun contributeCommentActionsActionsFragmentInjector(): DialogCommentMoreActions

    @ContributesAndroidInjector
    abstract fun contributeShareFragmentInjector(): ShareFragment

    @ContributesAndroidInjector
    abstract fun contributeEditPreviewFragmentInjector(): EditPreviewFragment

    @ContributesAndroidInjector
    abstract fun contributeFragmentPurchaseInjector(): FragmentPurchases

    @ContributesAndroidInjector
    abstract fun contributePlaylistsFragment(): PlaylistsFragment

    @ContributesAndroidInjector
    abstract fun contributeFragmentPlaylistDetails(): FragmentPlaylistDetails

    @ContributesAndroidInjector
    abstract fun contributeSaveToPlaylistFragmentInjector(): SaveToPlaylistFragment

    @ContributesAndroidInjector
    abstract fun contributeFragmentCreatePlaylistInjector(): FragmentCreatePlaylist

}
