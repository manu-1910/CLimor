package com.limor.app

import android.app.Activity
import android.app.Application
import android.app.Service
import android.provider.Settings
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessaging
import com.limor.app.di.AppInjector
import com.limor.app.di.components.AppComponent
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.service.PlayerBinder
import com.limor.app.util.AppState
import com.limor.app.util.CrashReportingTree
import com.novoda.merlin.MerlinsBeard
import com.smartlook.sdk.smartlook.Smartlook
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasServiceInjector
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

class App : Application(), HasActivityInjector, HasServiceInjector, LifecycleObserver, Configuration.Provider {
    @Inject
    lateinit var activityInjector: DispatchingAndroidInjector<Activity>
    @Inject
    lateinit var serviceInjector: DispatchingAndroidInjector<Service>
    @Inject
    lateinit var playerBinder: PlayerBinder

    private var realm: Realm? = null
    lateinit var firebaseAnalytics: FirebaseAnalytics
    var merlinsBeard: MerlinsBeard? = null

    override fun activityInjector(): DispatchingAndroidInjector<Activity> = activityInjector
    override fun serviceInjector(): AndroidInjector<Service> = serviceInjector


    var appComponent: AppComponent = AppInjector.init(this)
        @VisibleForTesting
        set

    companion object {
        lateinit var instance: App
        fun getDeviceId(): String {
            return Settings.Secure.getString(instance.contentResolver, Settings.Secure.ANDROID_ID)
        }

    }

    init {
        instance = this
    }


    override fun onCreate() {
        super.onCreate()
        appComponent.inject(this)

        realm = initRealm()

        //Connection
        merlinsBeard = MerlinsBeard.Builder().build(this)

        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        //Initialize Facebook SDK
        //FacebookSdk.sdkInitialize(this)

        initSmartLook()

        initLogging()

        getMessageToken()

//        AndroidAudioConverter.load(this, object : ILoadCallback {
//            override fun onSuccess() {
//                println("FFmpeg loaded success")
//            }
//
//            override fun onFailure(error: Exception) {
//                // FFmpeg is not supported by device
//                println("FFmpeg loaded error")
//            }
//        })
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_STOP)
    fun onAppBackground(){
        PrefsHandler.setAppState(App.instance, AppState.BACKGROUND)
    }

    @OnLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_START)
    fun onAppForeground(){
        PrefsHandler.setAppState(App.instance, AppState.FOREGROUND)
    }

    @OnLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_DESTROY)
    fun onAppDestroyed(){
        PrefsHandler.setAppState(App.instance, AppState.DESTROYED)
    }

    @OnLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_CREATE)
    fun onAppCreated(){
        PrefsHandler.setAppState(App.instance, AppState.CREATED)
        PrefsHandler.setAppLastState(App.instance, 3)
    }

    private fun getMessageToken() {

        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            Timber.d("Device Token--> $it")

        }

    }

    private fun initSmartLook() {
           Smartlook.setupAndStartRecording(BuildConfig.SMART_LOOK_API_KEY);
    }

    private fun initLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.plant(CrashReportingTree())
    }

    private fun initRealm(): Realm? {
        Realm.init(this)
        val realmConfiguration: RealmConfiguration = RealmConfiguration.Builder()
            .deleteRealmIfMigrationNeeded()
            //TODO: Encrypt database!!
            .build()
        Realm.setDefaultConfiguration(realmConfiguration)
        val realm = Realm.getDefaultInstance()

        return realm
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return if (BuildConfig.DEBUG) {
            Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.DEBUG)
                .build()
        } else {
            Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.ERROR)
                .build()
        }
    }
}