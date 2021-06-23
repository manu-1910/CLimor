package com.limor.app.di.modules

import com.limor.app.di.modules.fragments.*
import com.limor.app.scenes.authentication.SignActivity
import com.limor.app.scenes.main.MainActivity
import com.limor.app.scenes.main.fragments.CategoriesActivity
import com.limor.app.scenes.main.fragments.discover.DiscoverPodcastsByCategoryActivity
import com.limor.app.scenes.main.fragments.onboarding.OnBoardingActivity
import com.limor.app.scenes.main.fragments.player.AudioPlayerActivity
import com.limor.app.scenes.main.fragments.podcast.PodcastDetailsActivity
import com.limor.app.scenes.main.fragments.podcast.PodcastsByTagActivity
import com.limor.app.scenes.main.fragments.profile.ReportActivity
import com.limor.app.scenes.main.fragments.profile.UserFollowersFollowingsActivity
import com.limor.app.scenes.main.fragments.profile.UserProfileActivity
import com.limor.app.scenes.main.fragments.record.RecordActivity
import com.limor.app.scenes.main.fragments.settings.SettingsActivity
import com.limor.app.scenes.main.fragments.setup_patron.SetupPatronActivity
import com.limor.app.scenes.main_new.MainActivityNew
import com.limor.app.scenes.splash.SplashActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivitiesModule {

    @ContributesAndroidInjector
    abstract fun contributeSplashActivityInjector(): SplashActivity


    @ContributesAndroidInjector(modules = [
        (SignActivityFragmentsBuildersModule::class)
    ])
    abstract fun contributeSignActivityInjector(): SignActivity


    @ContributesAndroidInjector(modules = [
        (MainActivityFragmentsBuildersModule::class)
    ])
    abstract fun contributeMainActivityInjector(): MainActivity


    @ContributesAndroidInjector(modules = [
        (RecordActivityFragmentsBuildersModule::class)
    ])
    abstract fun contributeRecordActivityInjector(): RecordActivity


    @ContributesAndroidInjector(modules = [
        (PodcastDetailsActivityFragmentsBuildersModule::class)
    ])
    abstract fun contributePodcastDetailsActivityInjector(): PodcastDetailsActivity


    @ContributesAndroidInjector(modules = [
        (UserProfileActivityFragmentsBuildersModule::class)
    ])
    abstract fun contributeUserProfileActivityInjector(): UserProfileActivity


    @ContributesAndroidInjector(modules = [
        (PodcastsByTagActivityFragmentsBuildersModule::class)
    ])
    abstract fun contributePodcastsByTagActivityInjector(): PodcastsByTagActivity


    @ContributesAndroidInjector(modules = [
        (AudioPlayerActivityFragmentsBuildersModule::class)
    ])
    abstract fun contributeAudioPlayerActivityInjector(): AudioPlayerActivity


    @ContributesAndroidInjector
    abstract fun contributeUserReportActivityInjector(): ReportActivity


    @ContributesAndroidInjector(modules = [
        (SettingsActivityFragmentsBuildersModule::class)
    ])
    abstract fun contributeSettingsActivityInjector(): SettingsActivity

    @ContributesAndroidInjector(modules = [
        (OnBoardingActivityFragmentsBuildersModule::class)
    ])
    abstract fun contributeOnBoardingActivityInjector(): OnBoardingActivity

    @ContributesAndroidInjector(modules = [
        (SetupPatronActivityFragmentsBuildersModule::class)
    ])
    abstract fun contributeSetupPatronActivityInjector(): SetupPatronActivity

    @ContributesAndroidInjector(modules = [
        (SetupCategoriesActivityFragmentsBuildersModule::class)
    ])
    abstract fun contributeCategoriesActivityInjector(): CategoriesActivity

    @ContributesAndroidInjector(modules = [
        (DiscoverPodcastsByCategoryActivityFragmentsBuildersModule::class)
    ])
    abstract fun contributeDiscoverPodcastsByCategoryActivityInjector(): DiscoverPodcastsByCategoryActivity

    @ContributesAndroidInjector(modules = [
        (UserFollowersFollowingsFragmentsBuildersModule::class)
    ])
    abstract fun contributeUserFollowersFollingActivityInjector(): UserFollowersFollowingsActivity

    @ContributesAndroidInjector
    abstract fun contributeMainActivityNewActivityInjector(): MainActivityNew

}