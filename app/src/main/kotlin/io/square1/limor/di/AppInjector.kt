package io.square1.limor.di

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import dagger.android.AndroidInjection
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.HasSupportFragmentInjector
import io.square1.limor.App
import io.square1.limor.di.components.AppComponent
import io.square1.limor.di.components.DaggerAppComponent
import io.square1.limor.di.Injectable

class AppInjector {
    private fun AppInjector() {}

    companion object {
        fun init(app: App): AppComponent {

            app.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    handleActivity(activity)
                }

                override fun onActivityStarted(activity: Activity) {

                }

                override fun onActivityResumed(activity: Activity) {

                }

                override fun onActivityPaused(activity: Activity) {

                }

                override fun onActivityStopped(activity: Activity) {

                }

                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {

                }

                override fun onActivityDestroyed(activity: Activity) {

                }
            })

            return DaggerAppComponent.builder()
                .application(app)
                .build()
        }

        private fun handleActivity(activity: Activity) {
            if (activity is HasSupportFragmentInjector) {
                AndroidInjection.inject(activity)
            }
            if (activity is FragmentActivity) {
                activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
                    object : FragmentManager.FragmentLifecycleCallbacks() {
                        override fun onFragmentPreAttached(fm: FragmentManager, f: Fragment, context: Context) {
                            if (f is Injectable) {
                                AndroidSupportInjection.inject(f)
                            }
                            super.onFragmentPreAttached(fm, f, context)
                        }
                    }, true)
            }
        }
    }
}