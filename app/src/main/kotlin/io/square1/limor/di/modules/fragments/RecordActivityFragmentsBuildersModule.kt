package io.square1.limor.di.modules.fragments


import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.square1.limor.scenes.main.fragments.record.DraftsFragment
import io.square1.limor.scenes.main.fragments.record.EditFragment
import io.square1.limor.scenes.main.fragments.record.PublishFragment
import io.square1.limor.scenes.main.fragments.record.RecordFragment


@Module
abstract class RecordActivityFragmentsBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeRecordFragmentInjector(): RecordFragment

    @ContributesAndroidInjector
    abstract fun contributeEditFragmentInjector(): EditFragment

    @ContributesAndroidInjector
    abstract fun contributePublishFragmentInjector(): PublishFragment

    @ContributesAndroidInjector
    abstract fun contributeDraftsFragmentInjector(): DraftsFragment
}