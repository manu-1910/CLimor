package io.square1.limor

import android.app.Activity
import android.app.Application
import android.app.Service
import androidx.annotation.VisibleForTesting
import com.crashlytics.android.Crashlytics
import com.facebook.FacebookSdk
import com.google.firebase.analytics.FirebaseAnalytics
import com.novoda.merlin.MerlinsBeard
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasServiceInjector
import io.fabric.sdk.android.Fabric
import io.realm.Realm
import io.realm.RealmConfiguration
import io.square1.limor.di.AppInjector
import io.square1.limor.di.components.AppComponent
import javax.inject.Inject

class App : Application(), HasActivityInjector, HasServiceInjector {
    @Inject
    lateinit var activityInjector: DispatchingAndroidInjector<Activity>
    @Inject
    lateinit var serviceInjector: DispatchingAndroidInjector<Service>

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