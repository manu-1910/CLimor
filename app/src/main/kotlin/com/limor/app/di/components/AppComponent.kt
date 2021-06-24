package com.limor.app.di.components

import android.app.Application
import android.content.Context
import com.limor.app.App
import com.limor.app.di.modules.*
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [AndroidInjectionModule::class,
        ApplicationModule::class,
        ViewModelsModule::class,
        ActivitiesModule::class,
        DataModule::class,
        RemoteModule::class,
        StorageModule::class,
        MockModule::class
    ]
)
interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder
        fun build(): AppComponent
    }

    fun inject(app: App)
    fun context(): Context
}