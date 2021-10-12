package com.limor.app.di.modules

import com.limor.app.EditCastActivity
import com.limor.app.di.modules.fragments.*
import com.limor.app.dm.ui.ChatActivity
import com.limor.app.scenes.auth_new.AuthActivityNew
import com.limor.app.scenes.authentication.SignActivity
import com.limor.app.scenes.main.fragments.onboarding.OnBoardingActivity
import com.limor.app.scenes.main.fragments.player.AudioPlayerActivity
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


    @ContributesAndroidInjector(
        modules = [
            (SignActivityFragmentsBuildersModule::class)
        ]
    )
    abstract fun contributeSignActivityInjector(): SignActivity


    @ContributesAndroidInjector(
        modules = [
            (RecordActivityFragmentsBuildersModule::class)
        ]
    )
    abstract fun contributeRecordActivityInjector(): RecordActivity

    @ContributesAndroidInjector(
        modules = [
            (UserProfileActivityFragmentsBuildersModule::class)
        ]
    )
    abstract fun contributeUserProfileActivityInjector(): UserProfileActivity

    @ContributesAndroidInjector(
        modules = [
            (AudioPlayerActivityFragmentsBuildersModule::class)
        ]
    )
    abstract fun contributeAudioPlayerActivityInjector(): AudioPlayerActivity


    @ContributesAndroidInjector
    abstract fun contributeUserReportActivityInjector(): ReportActivity


    @ContributesAndroidInjector(
        modules = [
            (SettingsActivityFragmentsBuildersModule::class)
        ]
    )
    abstract fun contributeSettingsActivityInjector(): SettingsActivity

    @ContributesAndroidInjector(
        modules = [
            (OnBoardingActivityFragmentsBuildersModule::class)
        ]
    )
    abstract fun contributeOnBoardingActivityInjector(): OnBoardingActivity

    @ContributesAndroidInjector(
        modules = [
            (SetupPatronActivityFragmentsBuildersModule::class)
        ]
    )
    abstract fun contributeSetupPatronActivityInjector(): SetupPatronActivity

    @ContributesAndroidInjector(
        modules = [
            (UserFollowersFollowingsFragmentsBuildersModule::class)
        ]
    )
    abstract fun contributeUserFollowersFollingActivityInjector(): UserFollowersFollowingsActivity

    @ContributesAndroidInjector(
        modules = [
            MainActivityNewFragmentBuildersModule::class
        ]
    )
    abstract fun contributeMainActivityNewActivityInjector(): MainActivityNew

    @ContributesAndroidInjector
    abstract fun contributeAuthActivityNewInjector(): AuthActivityNew

    @ContributesAndroidInjector(
        modules = [
            ViewModelsModule::class
        ]
    )
    abstract fun contributeEditCastActivityInjector(): EditCastActivity


    @ContributesAndroidInjector(
        modules = [
            ViewModelsModule::class
        ]
    )
    abstract fun contributeChatActivityInjector(): ChatActivity
}