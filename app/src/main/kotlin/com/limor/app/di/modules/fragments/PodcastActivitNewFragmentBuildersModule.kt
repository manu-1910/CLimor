package com.limor.app.di.modules.fragments

import com.limor.app.scenes.main_new.fragments.comments.FragmentCommentReplies
import com.limor.app.scenes.main_new.fragments.comments.FragmentComments
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class PodcastActivitNewFragmentBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeCommentsFragmentInjector(): FragmentComments

    @ContributesAndroidInjector
    abstract fun contributeFragmentCommentRepliesInjector(): FragmentCommentReplies
}

