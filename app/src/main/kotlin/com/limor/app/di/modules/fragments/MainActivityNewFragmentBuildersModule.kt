package com.limor.app.di.modules.fragments

import com.limor.app.dm.ui.ChatSessionsFragment
import com.limor.app.dm.ui.ShareFragment
import com.limor.app.scenes.auth_new.fragments.FragmentCategories
import com.limor.app.scenes.auth_new.fragments.FragmentLanguages
import com.limor.app.scenes.main.fragments.discover.category.DiscoverAllCategoriesFragment
import com.limor.app.scenes.main.fragments.discover.category.DiscoverCategoryFragment
import com.limor.app.scenes.main.fragments.discover.discover.DiscoverFragment
import com.limor.app.scenes.main.fragments.discover.featuredcasts.DiscoverFeaturedCastsFragment
import com.limor.app.scenes.main.fragments.discover.hashtag.DiscoverHashtagFragment
import com.limor.app.scenes.main.fragments.discover.search.DiscoverSearchFragment
import com.limor.app.scenes.main.fragments.discover.suggestedpeople.DiscoverSuggestedPeopleFragment
import com.limor.app.scenes.main.fragments.profile.FragmentPurchases
import com.limor.app.scenes.main.fragments.profile.UserPatronFragmentNew
import com.limor.app.scenes.main.fragments.profile.UserProfileFragment
import com.limor.app.scenes.main.fragments.profile.casts.UserPodcastsFragmentNew
import com.limor.app.scenes.main_new.fragments.*
import com.limor.app.scenes.main_new.fragments.comments.FragmentComments
import com.limor.app.scenes.main_new.fragments.comments.FragmentCommentReplies
import com.limor.app.scenes.main_new.view.editpreview.EditPreviewFragment
import com.limor.app.scenes.notifications.Notification
import com.limor.app.scenes.patron.manage.fragment.*
import com.limor.app.scenes.patron.setup.FragmentPatronCategories
import com.limor.app.scenes.profile.DialogCommentMoreActions
import com.limor.app.scenes.profile.DialogUserReport
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainActivityNewFragmentBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeHomeFragmentInjector(): FragmentHomeNew

    @ContributesAndroidInjector
    abstract fun contributeDiscoverFragmentInjector(): DiscoverFragment

    @ContributesAndroidInjector
    abstract fun contributeDiscoverCategoryFragmentInjector(): DiscoverCategoryFragment

    @ContributesAndroidInjector
    abstract fun contributeDiscoverFeaturedCastsFragmentInjector(): DiscoverFeaturedCastsFragment

    @ContributesAndroidInjector
    abstract fun contributeDiscoverAllCategoriesFragmentInjector(): DiscoverAllCategoriesFragment

    @ContributesAndroidInjector
    abstract fun contributeDiscoverSuggestedPeopleFragmentInjector(): DiscoverSuggestedPeopleFragment

    @ContributesAndroidInjector
    abstract fun contributeDiscoverSearchFragmentInjector(): DiscoverSearchFragment

    @ContributesAndroidInjector
    abstract fun contributeUserProfileFragmentInjector(): UserProfileFragment

    @ContributesAndroidInjector
    abstract fun contributeDiscoverHashtagFragmentInjector(): DiscoverHashtagFragment

    @ContributesAndroidInjector
    abstract fun contributeUserPodcastsFragmentNewInjector(): UserPodcastsFragmentNew

    @ContributesAndroidInjector
    abstract fun contributeUserPatronFragmentNewInjector(): UserPatronFragmentNew

    @ContributesAndroidInjector
    abstract fun contributeExtendedPlayerFragmentInjector(): ExtendedPlayerFragment

    @ContributesAndroidInjector
    abstract fun contributeSmallPlayerFragmentInjector(): SmallPlayerFragment

    @ContributesAndroidInjector
    abstract fun contributeFragmentCommentsInjector(): FragmentComments

    @ContributesAndroidInjector
    abstract fun contributeFragmentCommentRepliesInjector(): FragmentCommentReplies

    @ContributesAndroidInjector
    abstract fun contributeNotificationsFragment(): Notification

    @ContributesAndroidInjector
    abstract fun contributePodcastActionsFragment(): DialogPodcastMoreActions

    @ContributesAndroidInjector
    abstract fun contributeUserReportFragment(): DialogUserReport

    @ContributesAndroidInjector
    abstract fun contributePodcastReportFragment(): DialogPodcastReportP2

    @ContributesAndroidInjector
    abstract fun contributeCommentActionsActionsFragmentInjector(): DialogCommentMoreActions

    @ContributesAndroidInjector
    abstract fun contributeShareFragmentInjector(): ShareFragment

    @ContributesAndroidInjector
    abstract fun contributeChatSessionsFragmentInjector(): ChatSessionsFragment

    @ContributesAndroidInjector
    abstract fun contributeManagePatronFragmentInjector(): ManagePatronFragment

    @ContributesAndroidInjector
    abstract fun contributeFragmentPatronCategories(): FragmentPatronCategories

    @ContributesAndroidInjector
    abstract fun contributeFragmentCastEarnings(): FragmentCastEarnings

    @ContributesAndroidInjector
    abstract fun contributeFragmentFragmentMyEarnings(): FragmentMyEarnings

    @ContributesAndroidInjector
    abstract fun contributeFragmentInviteFriends(): FragmentInviteFriends

    @ContributesAndroidInjector
    abstract fun contributeFragmentUpdatePatronCaregories(): FragmentUpdatePatronCategories

    @ContributesAndroidInjector
    abstract fun contributeEditPreviewFragmentInjector(): EditPreviewFragment

    @ContributesAndroidInjector
    abstract fun contributeFragmentPurchaseInjector(): FragmentPurchases

    @ContributesAndroidInjector
    abstract fun contributeFragmentCategories(): FragmentCategories

    @ContributesAndroidInjector
    abstract fun contributeFragmentLanguages(): FragmentLanguages

}

