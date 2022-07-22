package com.limor.app

import android.app.Activity
import android.app.Application
import android.app.Service
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessaging
import com.limor.app.di.AppInjector
import com.limor.app.di.components.AppComponent
import com.limor.app.dm.ChatManager
import com.limor.app.scenes.auth_new.util.PrefsHandler
import com.limor.app.scenes.main.fragments.record.RecordActivity
import com.limor.app.scenes.splash.SplashActivity
import com.limor.app.scenes.utils.LimorDialog
import com.limor.app.service.PlayerBinder
import com.limor.app.util.AppState
import com.limor.app.util.CrashReportingTree
import com.novoda.merlin.MerlinsBeard
import com.onesignal.OneSignal
import com.smartlook.sdk.smartlook.Smartlook
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasServiceInjector
import io.realm.*
import org.jetbrains.anko.layoutInflater
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

interface MediaPlayerHandler {
    fun interruptPlaying()
}

class App : Application(), HasActivityInjector, HasServiceInjector, LifecycleObserver,
    Application.ActivityLifecycleCallbacks {
    @Inject
    lateinit var activityInjector: DispatchingAndroidInjector<Activity>
    @Inject
    lateinit var serviceInjector: DispatchingAndroidInjector<Service>
    @Inject
    lateinit var playerBinder: PlayerBinder

    @Inject
    lateinit var chatManager: ChatManager

    private var realm: Realm? = null
    lateinit var firebaseAnalytics: FirebaseAnalytics
    var merlinsBeard: MerlinsBeard? = null

    var activity: Activity? = null

    private val errorShown = AtomicBoolean(false)

    override fun activityInjector(): DispatchingAndroidInjector<Activity> = activityInjector
    override fun serviceInjector(): AndroidInjector<Service> = serviceInjector

    private val mediaPlayerHandlers = mutableListOf<MediaPlayerHandler>()

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

    fun registerMediaPlayerHandler(mph: MediaPlayerHandler) {
        if (mediaPlayerHandlers.contains(mph)) {
            return
        }
        mediaPlayerHandlers.add(mph)
    }

    fun unregisterMediaPlayerHandler(mph: MediaPlayerHandler) {
        mediaPlayerHandlers.remove(mph)
    }

    fun interruptAllMediaPlayers() {
        mediaPlayerHandlers.forEach {
            println("interrupting $it")
            it.interruptPlaying()
        }
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

        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(BuildConfig.ONE_SIGNAL_APP_ID);

        registerActivityLifecycleCallbacks(this)
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
            .schemaVersion(1)
            .migration(Migration())
            .build()
        Realm.setDefaultConfiguration(realmConfiguration)
        val realm = Realm.getDefaultInstance()

        return realm
    }

    open class Migration : RealmMigration {

        override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
            val schema = realm.schema
            // This is the only known migration as of yet and it introduces a new field in the
            // Draft object called "Categories":
            if (oldVersion == 0L && newVersion == 1L) {
                val draftSchema = schema.get("RLMDraft")
                if (draftSchema == null) {
                    println("could not get schemas...")
                    return
                }

                val categorySchema = schema.create("RLMOnDeviceCategory",).apply {
                    addField("name", String::class.java)
                    setRequired("name", true)

                    addField("categoryId", Int::class.java)
                }

                draftSchema.addRealmListField("categories", categorySchema)
                draftSchema.addField("price", String::class.java)
            }
            println("Realm --> $oldVersion -> $newVersion")
        }
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {}

    override fun onActivityStarted(p0: Activity) {}

    override fun onActivityResumed(p0: Activity) {
        activity = p0
    }

    override fun onActivityPaused(p0: Activity) {}

    override fun onActivityStopped(p0: Activity) {}

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}

    override fun onActivityDestroyed(p0: Activity) {}

    fun showSomeThingWentWrongPopUp(){
        if(activity !is SplashActivity && !errorShown.getAndSet(true)){
            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                activity?.layoutInflater?.let {
                    LimorDialog(it).apply {
                        setTitle(R.string.oops)
                        setMessage(R.string.something_went_wrong_message)
                        setIcon(R.drawable.ic_alert)
                        addButton(R.string.close_app, activity !is RecordActivity) {
                            activity?.finishAndRemoveTask()
                        }
                        setDismissListener {
                            errorShown.set(false)
                        }
                        if(activity is RecordActivity){
                            addButton(R.string.cancel, true)
                        }
                    }.show()
                }
            }, 1500)
        }
    }

}