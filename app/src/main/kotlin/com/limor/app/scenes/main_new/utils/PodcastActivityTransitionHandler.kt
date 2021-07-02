package com.limor.app.scenes.main_new.utils

import android.graphics.Color
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.limor.app.R
import com.limor.app.scenes.main_new.PodcastsActivity
import java.lang.ref.WeakReference

class PodcastActivityTransitionHandler {
    companion object {
        fun setUpWindowTransition(activity: WeakReference<PodcastsActivity>) {
            activity.get()?.let {
                setWindowTransitionParams(it)
                setEnterTransition(it)
                setReturnTransition(it)
            }
        }

        private fun setWindowTransitionParams(activity: AppCompatActivity) {
            activity.window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
            activity.findViewById<View>(android.R.id.content).transitionName =
                "podcast_player_transition_label"
            activity.setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        }

        private fun setEnterTransition(activity: AppCompatActivity) {
            activity.window.sharedElementEnterTransition = MaterialContainerTransform().apply {
                addTarget(android.R.id.content)
                duration = 300L
                excludeTarget(R.id.toolbar, true);
                excludeTarget(android.R.id.statusBarBackground, true);
                excludeTarget(android.R.id.navigationBarBackground, true);
                scrimColor = Color.TRANSPARENT
                fadeMode = MaterialContainerTransform.FADE_MODE_CROSS;
                isElevationShadowEnabled = true

            }
        }

        private fun setReturnTransition(activity: AppCompatActivity) {
            activity.window.sharedElementReturnTransition = MaterialContainerTransform().apply {
                addTarget(android.R.id.content)
                duration = 250L
                containerColor = activity.resources.getColor(R.color.transparent)
                excludeTarget(R.id.toolbar, true);
                excludeTarget(android.R.id.statusBarBackground, true);
                excludeTarget(android.R.id.navigationBarBackground, true);
                scrimColor = Color.TRANSPARENT
                fadeMode = MaterialContainerTransform.FADE_MODE_CROSS;
                isElevationShadowEnabled = true;
            }
        }

        fun transitionListener(activity: WeakReference<PodcastsActivity>) = object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
            }

            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {
                activity.get()?.let{
                    if (p3 >= 0.20 && !it.isFinishing) {
                        it.binding.root.removeTransitionListener(this)
                        it.onBackPressed()
                    }
                }
            }

            override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
            }

            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {
            }
        }
    }
}