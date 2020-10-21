package com.limor.app.di.modules.fragments

import dagger.Module
import dagger.android.ContributesAndroidInjector
import com.limor.app.scenes.main.fragments.player.AudioPlayerFragment

@Module
abstract class AudioPlayerActivityFragmentsBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeAudioPlayerFragmentInjector(): AudioPlayerFragment
}