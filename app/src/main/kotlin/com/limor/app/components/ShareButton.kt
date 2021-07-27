package com.limor.app.components

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.limor.app.R

class ShareButton(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    @ColorInt
    private var sharedColor: Int

    @ColorInt
    private var notSharedColor: Int

    init {
        inflate(context, R.layout.view_share_button, this)

        context.obtainStyledAttributes(attrs, R.styleable.ShareButton).apply {
            sharedColor = getColor(
                R.styleable.RecastButton_recast_color,
                ContextCompat.getColor(context, R.color.textAccent)
            )

            notSharedColor = getColor(
                R.styleable.RecastButton_not_recasted_color,
                ContextCompat.getColor(context, R.color.white)
            )

        }.recycle()
    }

    private val shareBtn = findViewById<AppCompatImageView>(R.id.share_btn)

    var shared: Boolean = false
        set(value) {
            applyState(value)
            field = value
        }

    override fun setOnClickListener(l: OnClickListener?) {
        val wrapper = OnClickListener{
            l?.onClick(it)
        }
        shareBtn.setOnClickListener(wrapper)
    }

    fun applyState(isShared: Boolean){
        shareBtn.apply {
            if(isShared){
                imageTintList = ColorStateList.valueOf(sharedColor)
            } else{
                imageTintList = ColorStateList.valueOf(notSharedColor)
            }
        }
    }

    fun setStyle(sharedColor: Int = this.sharedColor,
                    notSharedColor: Int = this.notSharedColor){
        this.sharedColor = sharedColor
        this.notSharedColor = notSharedColor
    }

}