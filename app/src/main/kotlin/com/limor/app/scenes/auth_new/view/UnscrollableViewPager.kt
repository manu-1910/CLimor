package com.limor.app.scenes.auth_new.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

import androidx.viewpager.widget.ViewPager


class UnscrollableViewPager(context: Context, attrs: AttributeSet?) : ViewPager(context, attrs) {
    var isScrollable = false
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (isScrollable) {
            super.onTouchEvent(event)
        } else false
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return if (isScrollable) {
            super.onInterceptTouchEvent(event)
        } else false
    }

    fun setPagingEnabled(enabled: Boolean) {
        this.isScrollable = enabled
    }
}