package com.limor.app.scenes.main_new.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.INVALID_POINTER_ID
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.view.MotionEventCompat

class PlayerView : View {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var mPosX: Float = 0f
    private var mPosY: Float = 0f
    private var mLastTouchX: Float = 0f
    private var mLastTouchY: Float = 0f

    // The ‘active pointer’ is the one currently moving our object.
    private var mActivePointerId = INVALID_POINTER_ID
    private val scaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            mScaleFactor *= detector.scaleFactor

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f))

            invalidate()
            return true
        }
    }

    private val mScaleDetector = ScaleGestureDetector(context, scaleListener)
    private var mScaleFactor = 1f

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        // Let the ScaleGestureDetector inspect all events.
        mScaleDetector.onTouchEvent(ev)

        val action = MotionEventCompat.getActionMasked(ev)

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                MotionEventCompat.getActionIndex(ev).also { pointerIndex ->
                    // Remember where we started (for dragging)
                    mLastTouchX = MotionEventCompat.getX(ev, pointerIndex)
                    mLastTouchY = MotionEventCompat.getY(ev, pointerIndex)
                }

                // Save the ID of this pointer (for dragging)
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0)
            }

            MotionEvent.ACTION_MOVE -> {
                // Find the index of the active pointer and fetch its position
                val (x: Float, y: Float) =
                    MotionEventCompat.findPointerIndex(ev, mActivePointerId).let { pointerIndex ->
                        // Calculate the distance moved
                        MotionEventCompat.getX(ev, pointerIndex) to
                                MotionEventCompat.getY(ev, pointerIndex)
                    }

                mPosX += x - mLastTouchX
                mPosY += y - mLastTouchY

                invalidate()

                // Remember this touch position for the next move event
                mLastTouchX = x
                mLastTouchY = y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mActivePointerId = INVALID_POINTER_ID
            }
            MotionEvent.ACTION_POINTER_UP -> {

                MotionEventCompat.getActionIndex(ev).also { pointerIndex ->
                    MotionEventCompat.getPointerId(ev, pointerIndex)
                        .takeIf { it == mActivePointerId }
                        ?.run {
                            // This was our active pointer going up. Choose a new
                            // active pointer and adjust accordingly.
                            val newPointerIndex = if (pointerIndex == 0) 1 else 0
                            mLastTouchX = MotionEventCompat.getX(ev, newPointerIndex)
                            mLastTouchY = MotionEventCompat.getY(ev, newPointerIndex)
                            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex)
                        }
                }
            }
        }
        return true
    }
}