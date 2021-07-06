package com.limor.app.components

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.limor.app.R

class FollowButton(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    init {
        inflate(context, R.layout.view_follow_button, this)
    }

    private val followBtn = findViewById<Button>(R.id.button)

    var isFollowed: Boolean = false
        set(value) {
            applyStyle(value)
            field = value
        }

    init {
        followBtn.setTextColor(
            ContextCompat.getColor(
                context,
                R.color.textPrimary
            )
        )
    }

    override fun setOnClickListener(l: OnClickListener?) {
        val wrapper = OnClickListener {
            isFollowed = !isFollowed
            l?.onClick(it)
        }
        followBtn.setOnClickListener(wrapper)
    }

    private fun applyStyle(isFollowed: Boolean) {
        followBtn.apply {
            if (isFollowed) {
                setBackgroundColor(ContextCompat.getColor(
                    context,
                    R.color.main_button_background_follow
                ))
                setText(R.string.unfollow)
            } else {
                setBackgroundColor(ContextCompat.getColor(
                    context,
                    R.color.colorAccent
                ))
                setText(R.string.follow)
            }
        }
    }
}
