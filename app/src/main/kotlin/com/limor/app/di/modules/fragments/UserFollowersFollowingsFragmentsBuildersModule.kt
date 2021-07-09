package com.limor.app.di.modules.fragments


import com.limor.app.scenes.main.fragments.profile.*
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class UserFollowersFollowingsFragmentsBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeUserFollowersFragmentInjector(): UserFollowersFragment

    @ContributesAndroidInjector
    abstract fun contributeUserFollowingsFragmentInjector(): UserFollowingsFragment

    @ContributesAndroidInjector
    abstract fun contributeNewUserFollowersFragmentInjector(): UserFollowersFragmentNew

    @ContributesAndroidInjector
    abstract fun contributeNewUserFollowingsFragmentInjector(): UserFollowings

}

