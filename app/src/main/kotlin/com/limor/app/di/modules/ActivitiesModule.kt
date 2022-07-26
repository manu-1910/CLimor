package com.limor.app.di.modules

import com.limor.app.EditCastActivity
import com.limor.app.di.modules.fragments.*
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
import com.limor.app.scenes.patron.manage.ManagePatronActivity
import com.limor.app.scenes.patron.manage.fragment.ChangePriceActivity
import com.limor.app.scenes.patron.setup.PatronSetupActivity
import com.limor.app.scenes.patron.unipaas.UniPaasActivity
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

    @ContributesAndroidInjector
    abstract fun contributeEditCastActivityInjector(): EditCastActivity

    @ContributesAndroidInjector(
        modules = [
            (PatronSetupActivityFragmentsBuildersModule::class)
        ]
    )
    abstract fun contributeSetupPatronInjectorInjector(): PatronSetupActivity

    @ContributesAndroidInjector(
        modules = [
            (ManagePatronActivityFragmentBuilderModule::class)
        ]
    )
    abstract fun contributeManagePatronActivityInjector(): ManagePatronActivity

    @ContributesAndroidInjector(
        modules = [
            (UniPaasActivityFragmentBuilderModule::class)
        ]
    )
    abstract fun contributeUniPassActivityInjector(): UniPaasActivity

    @ContributesAndroidInjector
    abstract fun contributeChangePriceActivityInjector(): ChangePriceActivity

}