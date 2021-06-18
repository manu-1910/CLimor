package com.limor.app.components

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.limor.app.R

class CircleAuthorView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    init {
        inflate(context, R.layout.view_circle_image, this)
    }

    private val imageView = findViewById<ShapeableImageView>(R.id.image_view)
    private val containerView = findViewById<FrameLayout>(R.id.container)

    @ColorInt
    private var circleColor: Int = Color.BLACK

    init {
        context.obtainStyledAttributes(attrs, R.styleable.CircleAuthorView).apply {
            setCircleColor(getColor(R.styleable.CircleAuthorView_circleColor, circleColor))
            getDrawable(R.styleable.CircleAuthorView_imageSrc)?.let { setImageDrawable(it) }
        }.recycle()
    }

    fun setCircleColor(@ColorInt circleColor: Int) {
        this.circleColor = circleColor
        containerView.backgroundTintList = ColorStateList.valueOf(circleColor)
    }

    fun setImageDrawable(image: Drawable) {
        imageView.setImageDrawable(image)
    }

    fun setImageFromUrl(imageUrl: String) {
        Glide.with(this)
            .load(imageUrl)
            .into(imageView)
    }
}