package com.limor.app.components

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View.OnClickListener
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.limor.app.R
import com.limor.app.scenes.auth_new.util.colorStateList

class LikeButton(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    init {
        inflate(context, R.layout.view_like_button, this)
    }

    private val likeBtn = findViewById<AppCompatImageView>(R.id.like_btn)

    private var likedRes: Drawable
    private var unlikedRes: Drawable

    @ColorInt
    private var likedColor: Int

    @ColorInt
    private var unlikedColor: Int

    var isLiked: Boolean = false
        set(value) {
            applyStyle(value)
            field = value
        }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.LikeButton).apply {
            likedRes = getDrawable(R.styleable.LikeButton_liked_res) ?: ContextCompat.getDrawable(
                context,
                R.drawable.heart
            )!!

            unlikedRes =
                getDrawable(R.styleable.LikeButton_unliked_res) ?: ContextCompat.getDrawable(
                    context,
                    R.drawable.heart_outline
                )!!

            likedColor = getColor(
                R.styleable.LikeButton_liked_color,
                ContextCompat.getColor(context, R.color.textAccent)
            )

            unlikedColor = getColor(
                R.styleable.LikeButton_unliked_color,
                ContextCompat.getColor(context, R.color.white)
            )

        }.recycle()
    }

    override fun setOnClickListener(l: OnClickListener?) {
        val wrapper = OnClickListener {
            isLiked = !isLiked
            l?.onClick(it)
        }
        likeBtn.setOnClickListener(wrapper)
    }

    fun setLikeStyle(
        likedRes: Drawable = this.likedRes,
        unlikedRes: Drawable = this.unlikedRes,
        likedColor: Int = this.likedColor,
        unlikedColor: Int = this.unlikedColor
    ) {
        this.likedRes = likedRes
        this.unlikedRes = unlikedRes
        this.likedColor = likedColor
        this.unlikedColor = unlikedColor

        applyStyle(isLiked)
    }

    private fun applyStyle(isLiked: Boolean) {
        likeBtn.apply {
            if (isLiked) {
                setImageDrawable(likedRes)
                imageTintList = ColorStateList.valueOf(likedColor)
            } else {
                setImageDrawable(unlikedRes)
                imageTintList = ColorStateList.valueOf(unlikedColor)
            }
        }
    }
}
