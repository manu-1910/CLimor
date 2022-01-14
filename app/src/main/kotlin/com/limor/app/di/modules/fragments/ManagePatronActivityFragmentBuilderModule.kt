package com.limor.app.di.modules.fragments

import com.limor.app.playlists.SaveToPlaylistFragment
import com.limor.app.scenes.main_new.fragments.ExtendedPlayerFragment
import com.limor.app.scenes.main_new.fragments.SmallPlayerFragment
import com.limor.app.scenes.patron.manage.fragment.*
import com.limor.app.scenes.patron.setup.FragmentPatronCategories
import com.limor.app.scenes.utils.FragmentCreatePlaylist
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ManagePatronActivityFragmentBuilderModule {

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
    abstract fun contributeFragmentUpdatePatronCategories(): FragmentUpdatePatronCategories

    @ContributesAndroidInjector
    abstract fun contributeFragmentInviteUsers(): FragmentInviteUsers

    @ContributesAndroidInjector
    abstract fun contributeExtendedPlayerFragmentInjector(): ExtendedPlayerFragment

    @ContributesAndroidInjector
    abstract fun contributeSmallPlayerFragmentInjector(): SmallPlayerFragment

    @ContributesAndroidInjector
    abstract fun contributeSaveToPlaylistFragmentInjector(): SaveToPlaylistFragment

    @ContributesAndroidInjector
    abstract fun contributeFragmentCreatePlaylistInjector(): FragmentCreatePlaylist

}