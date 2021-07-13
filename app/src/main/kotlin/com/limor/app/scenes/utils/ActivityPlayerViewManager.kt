package com.limor.app.scenes.utils

import android.content.Context
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.limor.app.R
import com.limor.app.databinding.ContainerWithSwipeablePlayerBinding
import com.limor.app.extensions.makeGone
import com.limor.app.extensions.makeVisible
import com.limor.app.scenes.main_new.fragments.ExtendedPlayerFragment
import com.limor.app.scenes.main_new.fragments.SmallPlayerFragment
import com.limor.app.service.PlayerBinder
import timber.log.Timber
import kotlin.math.abs

class ActivityPlayerViewManager(
    appContext: Context,
    private val fragmentManager: FragmentManager,
    private val playerBinding: ContainerWithSwipeablePlayerBinding
) : PlayerViewManager, MotionLayout.TransitionListener {

    private var currentFragment: Fragment? = null
    private var currentArgs: PlayerViewManager.PlayerArgs? = null
    private var isPlayerVisible: Boolean = false
    private var lastTransitionProgress = 0f
    private val _playerBinder: PlayerBinder = PlayerBinder(appContext)

    init {
        playerBinding.motionLayout.apply {
            setTransitionListener(this@ActivityPlayerViewManager)
        }
        playerBinding.playerContainer.makeGone()
    }

    override fun isPlayerVisible() = isPlayerVisible

    override fun showPlayer(args: PlayerViewManager.PlayerArgs) {
        currentArgs = args

        _playerBinder.start(args.cast)

        when (args.playerType) {
            PlayerViewManager.PlayerType.SMALL -> {
                playerBinding.playerContainer.makeVisible()
                playerBinding.motionLayout.apply {
                    transitionToStart()
                }
            }
            PlayerViewManager.PlayerType.EXTENDED -> {
                playerBinding.playerContainer.makeVisible()
                playerBinding.motionLayout.apply {
                    transitionToEnd()
                }
            }
            PlayerViewManager.PlayerType.TINY -> TODO()
        }

        isPlayerVisible = true
    }

    override fun hidePlayer() {
        playerBinding.playerContainer.makeGone()
        isPlayerVisible = false
        _playerBinder.stop()
    }

    override fun onTransitionChange(
        motionLayout: MotionLayout,
        startId: Int,
        endId: Int,
        progress: Float
    ) {
        if (progress - lastTransitionProgress > 0) {
            // from start to end
            val atEnd = abs(progress - 1f) < 0.9f
            if (atEnd && currentFragment !is ExtendedPlayerFragment) {
                val transaction = fragmentManager.beginTransaction()
                transaction
                    .setCustomAnimations(R.animator.show, 0)
                currentFragment = ExtendedPlayerFragment.newInstance(currentArgs!!.cast).also {
                    transaction
                        .replace(playerBinding.playerContainer.id, it)
                        .commit()
                }
            }
        } else {
            // from end to start
            val atStart = progress < 0.2f
            if (atStart && currentFragment !is SmallPlayerFragment) {
                val transaction = fragmentManager.beginTransaction()
                transaction
                    .setCustomAnimations(R.animator.show, 0)
                currentFragment = SmallPlayerFragment.newInstance(currentArgs!!.cast).also {
                    transaction
                        .replace(playerBinding.playerContainer.id, it)
                        .commit()
                }
            }
        }
        lastTransitionProgress = progress
    }

    override fun getPlayerBinder(): PlayerBinder {
        return _playerBinder
    }

    fun stop() {
        _playerBinder.stop()
    }

    override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) = Unit
    override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) = Unit
    override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) = Unit
}
