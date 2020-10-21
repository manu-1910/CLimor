package com.limor.app.di.modules.fragments

import com.limor.app.scenes.main.fragments.player.AudioPlayerFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AudioPlayerActivityFragmentsBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeAudioPlayerFragmentInjector(): AudioPlayerFragment
}