package com.limor.app.di.modules.fragments

import com.limor.app.scenes.main.fragments.discover.category.DiscoverAllCategoriesFragment
import com.limor.app.scenes.main.fragments.discover.category.DiscoverCategoryFragment
import com.limor.app.scenes.main.fragments.discover.discover.DiscoverFragment
import com.limor.app.scenes.main.fragments.discover.featuredcasts.DiscoverFeaturedCastsFragment
import com.limor.app.scenes.main.fragments.discover.search.DiscoverSearchFragment
import com.limor.app.scenes.main.fragments.discover.suggestedpeople.DiscoverSuggestedPeopleFragment
import com.limor.app.scenes.main.fragments.profile.UserProfileFragment
import com.limor.app.scenes.main_new.fragments.FragmentHomeNew
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainActivityNewFragmentBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeHomeFragmentInjector(): FragmentHomeNew

    @ContributesAndroidInjector
    abstract fun contributeDiscoverFragmentInjector(): DiscoverFragment

    @ContributesAndroidInjector
    abstract fun contributeDiscoverCategoryFragmentInjector(): DiscoverCategoryFragment

    @ContributesAndroidInjector
    abstract fun contributeDiscoverFeaturedCastsFragmentInjector(): DiscoverFeaturedCastsFragment

    @ContributesAndroidInjector
    abstract fun contributeDiscoverAllCategoriesFragmentInjector(): DiscoverAllCategoriesFragment

    @ContributesAndroidInjector
    abstract fun contributeDiscoverSuggestedPeopleFragmentInjector(): DiscoverSuggestedPeopleFragment

    @ContributesAndroidInjector
    abstract fun contributeDiscoverSearchFragmentInjector(): DiscoverSearchFragment

    @ContributesAndroidInjector
    abstract fun contributeUserProfileFragmentInjector(): UserProfileFragment

}

