package com.limor.app.di.modules.fragments

import com.limor.app.scenes.main.fragments.FeedItemsListFragment
import com.limor.app.scenes.main.fragments.ProfileFragment
import com.limor.app.scenes.main.fragments.profile.JoinToPatronFragment
import com.limor.app.scenes.main.fragments.profile.UserLikedPodcastsFragment
import com.limor.app.scenes.main.fragments.profile.UserPatronPodcastsFragment
import com.limor.app.scenes.main.fragments.profile.UserPodcastsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class UserProfileActivityFragmentsBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeUserProfileFragmentInjector(): ProfileFragment

    @ContributesAndroidInjector
    abstract fun contributeFeedFragmentInjector(): FeedItemsListFragment

    @ContributesAndroidInjector
    abstract fun contributeUserPodcastsFragmentInjector(): UserPodcastsFragment

    @ContributesAndroidInjector
    abstract fun contributeUserLikedPodcastsFragmentInjector(): UserLikedPodcastsFragment

    @ContributesAndroidInjector
    abstract fun contributeJoinToPatronFragmentInjector(): JoinToPatronFragment

    @ContributesAndroidInjector
    abstract fun contributeUserPatronPodcastsFragmentInjector(): UserPatronPodcastsFragment
}

