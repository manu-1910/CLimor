package io.square1.limor.di.modules.fragments

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.square1.limor.scenes.main.fragments.FeedFragment
import io.square1.limor.scenes.main.fragments.ProfileFragment

@Module
abstract class UserProfileActivityFragmentsBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeUserProfileFragmentInjector(): ProfileFragment

    @ContributesAndroidInjector
    abstract fun contributeFeedFragmentInjector(): FeedFragment
}