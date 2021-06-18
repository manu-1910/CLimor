package com.limor.app.components

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.graphics.drawable.toBitmap
import com.limor.app.R
import com.limor.app.components.util.CenterCropDrawable

class CroppedCircleAuthorView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    init {
        context.obtainStyledAttributes(attrs, R.styleable.CroppedCircleAuthorView).apply {
            val drawableResId = getResourceId(R.styleable.CroppedCircleAuthorView_imageSrc, -1)
            if (drawableResId!= -1) {
                setAvatarIcon(drawableResId)
            }
        }.recycle()
    }

    private var type: Type = Type.SMALL
    private var avatarImageDrawable: Drawable? = null
    private var avatarImageBitmap: Bitmap? = null
    private lateinit var bitmap: Bitmap
    private lateinit var bitmapCanvas: Canvas
    private lateinit var plusIconCircleDrawParams: CircleDrawParams
    private val transparentCircleCropPaint = Paint().apply {
        color = Color.TRANSPARENT
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        isAntiAlias = true
    }
    private val plusCirclePaint = Paint().apply {
        color = Color.parseColor("#FF5A4E")
        isAntiAlias = true
    }
    private val plusIconPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.white)
        isAntiAlias = true
    }

    /**
     * Don't forget to call [refreshCanvas] to see changes
     */
    fun setAvatarIcon(@DrawableRes drawableResId: Int) {
        avatarImageDrawable = CenterCropDrawable(
            RoundedBitmapDrawableFactory.create(
                resources,
                BitmapFactory.decodeResource(resources, drawableResId)
            ).apply { isCircular = true }
        )
    }

    /**
     * Don't forget to call [refreshCanvas] to see changes
     */
    fun setType(type: Type) {
        this.type = type
    }

    fun refreshCanvas(invalidate: Boolean = true) {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.TRANSPARENT)
        bitmapCanvas = Canvas(bitmap)

        val plusIconCircleRadius = (height * type.plusIconCircleRatio) / 2
        plusIconCircleDrawParams = CircleDrawParams(
            x = width / 2f,
            y = height - plusIconCircleRadius,
            radius = plusIconCircleRadius
        )

        val imageSize = (height * type.avatarCircleRatio).toInt()
        avatarImageBitmap = avatarImageDrawable?.toBitmap(imageSize, imageSize)

        if (invalidate) {
            invalidate()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        refreshCanvas(invalidate = false)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw avatar image bitmap
        avatarImageBitmap?.let {
            val imageStartPoint = (width - it.width) / 2f
            bitmapCanvas.drawBitmap(it, imageStartPoint, 0f, null)
        }

        if (type != Type.NO_PLUS_ICON) {
            // Draw transparent overlay circle
            bitmapCanvas.drawCircle(
                plusIconCircleDrawParams.x,
                plusIconCircleDrawParams.y,
                plusIconCircleDrawParams.radius * type.transparentCircleOverlayRatio,
                transparentCircleCropPaint
            )
            // Draw "Plus" icon circle
            bitmapCanvas.drawCircle(
                plusIconCircleDrawParams.x,
                plusIconCircleDrawParams.y,
                plusIconCircleDrawParams.radius,
                plusCirclePaint
            )
            // Draw "Plus" icon horizontal rect
            val left1 = plusIconCircleDrawParams.x - (plusIconCircleDrawParams.radius / 2)
            val top1 = plusIconCircleDrawParams.y - (plusIconCircleDrawParams.radius / 8)
            val right1 = plusIconCircleDrawParams.x + (plusIconCircleDrawParams.x - left1)
            val bottom1 = plusIconCircleDrawParams.y + (plusIconCircleDrawParams.y - top1)
            bitmapCanvas.drawRoundRect(left1, top1, right1, bottom1, 4f, 4f, plusIconPaint)
            // Draw "Plus" icon vertical rect
            val left2 = plusIconCircleDrawParams.x - (plusIconCircleDrawParams.radius / 8)
            val top2 = plusIconCircleDrawParams.y - (plusIconCircleDrawParams.radius / 2)
            val right2 = plusIconCircleDrawParams.x + (plusIconCircleDrawParams.x - left2)
            val bottom2 = plusIconCircleDrawParams.y + (plusIconCircleDrawParams.y - top2)
            bitmapCanvas.drawRoundRect(left2, top2, right2, bottom2, 4f, 4f, plusIconPaint)
        }

        canvas.drawBitmap(bitmap, 0f, 0f, null)
    }

    private data class CircleDrawParams(
        val x: Float,
        val y: Float,
        val radius: Float
    )

    /**
     * @param avatarCircleRatio difference between view height and avatar circle size
     * @param plusIconCircleRatio difference between view height and "plus" icon circle size
     * @param transparentCircleOverlayRatio difference between "plus" icon circle size and "plus"
     * transparent circle overlay size
     */
    enum class Type(
        val avatarCircleRatio: Float,
        val plusIconCircleRatio: Float,
        val transparentCircleOverlayRatio: Float = 1.3f
    ) {
        BIG(avatarCircleRatio = 0.685f, plusIconCircleRatio = 0.455f),
        SMALL(avatarCircleRatio = 0.859f, plusIconCircleRatio = 0.314f),
        NO_PLUS_ICON(1f, -1f)
    }
}