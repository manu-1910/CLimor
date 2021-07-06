package com.limor.app.components

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import com.limor.app.R
import com.limor.app.scenes.auth_new.util.colorStateList

class LikeButton(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    init {
        inflate(context, R.layout.view_like_button, this)
    }

    private val likeBtn = findViewById<AppCompatImageView>(R.id.like_btn)

    var isLiked: Boolean = false
        set(value) {
            applyStyle(value)
            field = value
        }

    override fun setOnClickListener(l: OnClickListener?) {
        val wrapper = OnClickListener {
            isLiked = !isLiked
            l?.onClick(it)
        }
        likeBtn.setOnClickListener(wrapper)
    }

    private fun applyStyle(isLiked: Boolean) {
        likeBtn.apply {
            if (isLiked) {
                setImageResource(R.drawable.heart)
                imageTintList = colorStateList(context, R.color.textAccent)
            } else {
                setImageResource(R.drawable.heart_outline)
                imageTintList = colorStateList(context, R.color.white)
            }
        }
    }
}
