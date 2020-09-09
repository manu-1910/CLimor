package io.square1.limor.di.modules.fragments

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.square1.limor.scenes.main.fragments.player.AudioPlayerFragment

@Module
abstract class AudioPlayerActivityFragmentsBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeAudioPlayerFragmentInjector(): AudioPlayerFragment
}