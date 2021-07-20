package com.limor.app

import android.app.Activity
import android.app.Application
import android.app.Service
import androidx.annotation.VisibleForTesting
import com.facebook.FacebookSdk
import com.google.firebase.analytics.FirebaseAnalytics
import com.limor.app.di.AppInjector
import com.limor.app.di.components.AppComponent
import com.limor.app.service.PlayerBinder
import com.limor.app.util.CrashReportingTree
import com.novoda.merlin.MerlinsBeard
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasServiceInjector
import io.realm.Realm
import io.realm.RealmConfiguration
import timber.log.Timber
import javax.inject.Inject

class App : Application(), HasActivityInjector, HasServiceInjector {
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
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        appComponent.inject(this)

        realm = initRealm()

        //Connection
        merlinsBeard = MerlinsBeard.Builder().build(this)

        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        //Initialize Facebook SDK
        FacebookSdk.sdkInitialize(this)

        initSmartLook()

        initLogging()

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


    }

    private fun initSmartLook() {
     //   Smartlook.setupAndStartRecording(BuildConfig.SMART_LOOK_API_KEY);
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
}