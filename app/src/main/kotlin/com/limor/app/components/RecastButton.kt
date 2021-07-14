package com.limor.app.components

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.limor.app.R

class RecastButton(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs)  {

    @ColorInt
    private var recastedColor: Int

    @ColorInt
    private var notRecastedColor: Int

    init {
        inflate(context, R.layout.view_recast_button, this)

        context.obtainStyledAttributes(attrs, R.styleable.RecastButton).apply {
            recastedColor = getColor(
                R.styleable.RecastButton_recast_color,
                ContextCompat.getColor(context, R.color.textAccent)
            )

            notRecastedColor = getColor(
                R.styleable.RecastButton_not_recasted_color,
                ContextCompat.getColor(context, R.color.white)
            )

        }.recycle()
    }

    private val recastBtn = findViewById<AppCompatImageView>(R.id.recast_btn)

    var recasted: Boolean = false
        set(value) {
            applyStyle(value)
            field = value
        }

    override fun setOnClickListener(l : OnClickListener?){
        val wrapper = OnClickListener {
            l?.onClick(it)
        }
        recastBtn.setOnClickListener(wrapper)
    }

    fun setStyle(recastedColor: Int = this.recastedColor,
                 notRecastedColor: Int = this.notRecastedColor){
        this.recastedColor = recastedColor
        this.notRecastedColor = notRecastedColor
    }

    fun applyStyle(isRecasted: Boolean){
        recastBtn.apply {
            if(isRecasted){
                imageTintList = ColorStateList.valueOf(recastedColor)
            } else{
                imageTintList = ColorStateList.valueOf(notRecastedColor)
            }
        }
    }

}