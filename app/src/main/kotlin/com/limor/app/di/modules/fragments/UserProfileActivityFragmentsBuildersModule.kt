package com.limor.app.di.modules.fragments

import com.limor.app.scenes.main.fragments.ProfileFragment
import com.limor.app.scenes.main.fragments.profile.*
import com.limor.app.scenes.main.fragments.profile.casts.UserPodcastsFragmentNew
import com.limor.app.scenes.main_new.fragments.ExtendedPlayerFragment
import com.limor.app.scenes.main_new.fragments.SmallPlayerFragment
import com.limor.app.scenes.profile.DialogUserProfileActions
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class UserProfileActivityFragmentsBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeUserProfileFragmentInjector(): UserProfileFragment

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
    abstract fun contributeExtendedPlayerFragmentInjector(): ExtendedPlayerFragment

    @ContributesAndroidInjector
    abstract fun contributeSmallPlayerFragmentInjector(): SmallPlayerFragment
}
