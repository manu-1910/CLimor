package com.limor.app.scenes.utils

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

class ActivityPlayerViewManager(
    private val fragmentManager: FragmentManager,
    private val playerBinding: ContainerWithSwipeablePlayerBinding,
    private val playerBinder: PlayerBinder
) : PlayerViewManager, MotionLayout.TransitionListener {

    private var currentFragment: Fragment? = null
    private var currentArgs: PlayerViewManager.PlayerArgs? = null
    private var isPlayerVisible: Boolean = false

    init {
        playerBinding.motionLayout.apply {
            setTransitionListener(this@ActivityPlayerViewManager)
        }
        playerBinding.playerContainer.makeGone()
    }

    override fun isPlayerVisible() = isPlayerVisible

    override fun showPlayer(args: PlayerViewManager.PlayerArgs) {
        currentArgs = args
        Timber.d("Clicked inside ${currentArgs}")
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
        stop()
    }

    fun stop() {
        playerBinder.stop()
    }

    private inline fun <reified T> showFragment(fragment: () -> Fragment) {
        if (currentFragment is T) {
            return
        }
        val targetFragment = fragment()
        currentFragment = fragment()
        fragmentManager.beginTransaction().also {
            it.setCustomAnimations(R.animator.show, 0)
            it.replace(playerBinding.playerContainer.id, targetFragment)
            it.commit()
        }
    }

    private fun showExtendedPlayer() {
        val args = currentArgs!!
        showFragment<ExtendedPlayerFragment> {
            ExtendedPlayerFragment.newInstance(args.castId, !args.maximizedFromMiniPlayer)
        }
    }

    private fun showMiniPlayer(){
        showFragment<SmallPlayerFragment> {
            SmallPlayerFragment.newInstance(currentArgs!!.castId)
        }
    }

    private fun toggleFragments(shouldShowMiniPlayer: Boolean) {
        if (shouldShowMiniPlayer) {
            showMiniPlayer()
        } else {
            showExtendedPlayer()
        }
    }

    override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
        val isAtStart = currentId == R.id.start
        // "at start" means the bottom mini player, check R.xml.container_transition_player_scene
        // where the @+id/start ConstraintSet has the mini player height and position, so when the
        // currentId is the "start" ID we need to be showing the mini player
        toggleFragments(isAtStart)
    }

    override fun onTransitionStarted(motionLayout: MotionLayout?, startId: Int, endId: Int) {
        // All transitions should always start with the extended player, there are these 2 cases:
        // 1. from mini player -> extended player
        // 2. from extended player -> mini player
        //
        // Case #2 is clear, it should be showing the maximized extended player and move to the
        // mini player bounds until the animation is completed at which point the other motion
        // callback (onTransitionCompleted) will ensure the mini player would be shown.
        //
        // Case #1 too needs to start with the Extended player. This is the case when the mini
        // player is shown and we need to maximize it to the extended player. For effect we
        // immediately show the extended player and let it maximize to its bounds. So there too
        // the start fragment should be the Extended player.
        //
        // Bottom-line: always start from the ExtendedPlayerFragment
        toggleFragments(false)
    }

    override fun onTransitionChange(motionLayout: MotionLayout, startId: Int, endId: Int, progress: Float) = Unit
    override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) = Unit
}
