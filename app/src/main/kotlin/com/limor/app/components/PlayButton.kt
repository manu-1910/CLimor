package com.limor.app.components

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View.OnClickListener
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.limor.app.R

class PlayButton(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    init {
        inflate(context, R.layout.view_play_button, this)
    }

    private val playBtn = findViewById<AppCompatImageView>(R.id.play_btn)

    private var playRes: Drawable
    private var pauseRes: Drawable

    var isPaused: Boolean = true
        set(value) {
            applyStyle(value)
            field = value
        }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.PlayButton).apply {
            playRes =
                getDrawable(R.styleable.PlayButton_play_res) ?: ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_play
                )!!

            pauseRes =
                getDrawable(R.styleable.PlayButton_pause_res) ?: ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_pause
                )!!

        }.recycle()
    }

    override fun setOnClickListener(l: OnClickListener?) {
        val wrapper = OnClickListener {
            isPaused = !isPaused
            l?.onClick(it)
        }
        playBtn.setOnClickListener(wrapper)
    }

    private fun applyStyle(isPaused: Boolean) {
        playBtn.apply {
            if (isPaused) {
                setImageDrawable(playRes)
            } else {
                setImageDrawable(pauseRes)
            }
        }
    }
}
