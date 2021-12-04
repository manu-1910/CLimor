package com.limor.app.di.components

import android.app.Application
import android.content.Context
import androidx.work.ListenableWorker

import com.limor.app.App
import com.limor.app.di.modules.*
import com.limor.app.service.VoiceCommentUploadService
import dagger.*
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton
import kotlin.reflect.KClass
import dagger.android.ContributesAndroidInjector

@Singleton
@Component(
    modules = [AndroidInjectionModule::class,
        ApplicationModule::class,
        ViewModelsModule::class,
        ActivitiesModule::class,
        DataModule::class,
        RemoteModule::class,
        StorageModule::class,
        ApolloModule::class,
        MockModule::class,
        ServicesModule::class,
        DatabaseModule::class
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

@MapKey
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class WorkerKey(val value: KClass<out ListenableWorker>)

@Module
abstract class ServicesModule {
    @ContributesAndroidInjector
    abstract fun provideVoiceCommentUploadService(): VoiceCommentUploadService
}