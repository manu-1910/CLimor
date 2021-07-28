package com.limor.app.scenes.utils.visualizer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.limor.app.R
import com.limor.app.extensions.px
import com.limor.app.scenes.utils.Commons
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

open class Visualizer : View {

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        init()
        loadAttribute(context, attrs)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init()
        loadAttribute(context, attrs)
    }

    var ampNormalizer: (Int) -> Int = { sqrt(it.toFloat()).toInt() }

    protected var amps = mutableListOf<Int>()
    protected var maxAmp = 10000f
    protected var approximateBarDuration = 50
    protected var spaceBetweenBar = 2f
    protected var cursorPosition = 0f
    protected var tickPerBar = 1
    protected var tickDuration = 1
    protected var tickCount = 0
    protected var barDuration = 1000
    protected var barWidth = 2f
        set(value) {
            if (field > 0) {
                field = value
                this.backgroundBarPrimeColor.strokeWidth = value
                this.loadedBarPrimeColor.strokeWidth = value
            }
        }
    private var maxVisibleBars = 0
    private lateinit var loadedBarPrimeColor: Paint
    private lateinit var backgroundBarPrimeColor: Paint
    private lateinit var timelineBackgroundColor: Paint

    private lateinit var timeCodePaint: Paint
    private var drawTimeCodes = true
    private var drawStartPosition = 0.5f

    private fun init() {
        backgroundBarPrimeColor = Paint()
        this.backgroundBarPrimeColor.color = context.getColorCompat(R.color.grayWave)
        this.backgroundBarPrimeColor.strokeCap = Paint.Cap.ROUND
        this.backgroundBarPrimeColor.strokeWidth = barWidth

        loadedBarPrimeColor = Paint()
        this.loadedBarPrimeColor.color = context.getColorCompat(R.color.orangeColor)
        this.loadedBarPrimeColor.strokeCap = Paint.Cap.ROUND
        this.loadedBarPrimeColor.strokeWidth = barWidth

        timelineBackgroundColor = Paint()
        this.timelineBackgroundColor.color = context.getColorCompat(R.color.white)
        this.backgroundBarPrimeColor.strokeCap = Paint.Cap.ROUND

        timeCodePaint = Paint()
        timeCodePaint.textSize = context.resources.getDimension(R.dimen.textSize14)
        timeCodePaint.isAntiAlias = true
        timeCodePaint.typeface = ResourcesCompat.getFont(context, R.font.roboto_medium)
        timeCodePaint.color =
            resources.getColor(R.color.textSecondary) //This is the text of the time topbar


    }

    private fun loadAttribute(context: Context, attrs: AttributeSet?) {
        val typedArray = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.iiVisu, 0, 0
        )
        try {
            spaceBetweenBar =
                typedArray.getDimension(R.styleable.iiVisu_spaceBetweenBar, context.dpToPx(2f))
            approximateBarDuration =
                typedArray.getInt(R.styleable.iiVisu_approximateBarDuration, 50)

            barWidth = typedArray.getDimension(R.styleable.iiVisu_barWidth, 1.3f)
            maxAmp = typedArray.getFloat(R.styleable.iiVisu_maxAmp, 40f)

            timelineBackgroundColor.apply {
                color = typedArray.getColor(
                    R.styleable.iiVisu_timelineBackgroundColor,
                    timelineBackgroundColor.color
                )
            }

            loadedBarPrimeColor.apply {
                strokeWidth = barWidth
                color = typedArray.getColor(
                    R.styleable.iiVisu_loadedBarPrimeColor,
                    context.getColorCompat(R.color.orangeColor)
                )
            }
            backgroundBarPrimeColor.apply {
                strokeWidth = barWidth
                color = typedArray.getColor(
                    R.styleable.iiVisu_backgroundBarPrimeColor,
                    context.getColorCompat(R.color.grayWave)
                )
            }

            drawTimeCodes = typedArray.getBoolean(
                R.styleable.iiVisu_drawTimeCodes,
                true
            )

            drawStartPosition = typedArray.getFloat(
                R.styleable.iiVisu_drawStartPosition,
                0.5f
            )

        } finally {
            typedArray.recycle()
        }
    }

    protected val currentDuration: Long
        get() = getTimeStamp(cursorPosition)

    override fun onDraw(canvas: Canvas) {
        val barDuration = tickPerBar * tickDuration
        val barsPerSecond = (1000.0 / barDuration).toLong()

        /* canvas.drawRect(
             0f,
             0f,
             measuredWidth.toFloat(),
             24.px.toFloat(),
             timelineBackgroundColor
         )*/
        canvas.drawRect(
            0f,
            measuredHeight.toFloat() - 24.px,
            measuredWidth.toFloat(),
            measuredHeight.toFloat(),
            timelineBackgroundColor
        )

        if (amps.isNotEmpty()) {
            var count = 0
            for (i in getStartBar() until getEndBar()) {
                if (i % barsPerSecond == 0L) {
                    val second = i / barsPerSecond
                    if (drawTimeCodes) {
                        canvas.drawText(
                            Commons.getLengthFromEpochForPlayer(TimeUnit.SECONDS.toMillis(second)),
                            getDrawStart() - (getBarPosition() - i) * (barWidth + spaceBetweenBar),
                            height.toFloat() - 5f,
                            timeCodePaint
                        )
                    }
                    count++
                }
                val startX = getDrawStart() - (getBarPosition() - i) * (barWidth + spaceBetweenBar)
                drawStraightBar(canvas, startX, getBarHeightAt(i).toInt(), getBaseLine())
            }
        }

        // Top background

        super.onDraw(canvas)
    }

    private fun getDrawStart(): Float {
        return drawStartPosition * width
    }

    private fun drawStraightBar(canvas: Canvas, startX: Float, height: Int, baseLine: Int) {
        val startY = baseLine + (height / 2).toFloat()
        val stopY = startY - height
        if (startX <= getDrawStart()) {
            canvas.drawLine(startX, startY, startX, stopY, loadedBarPrimeColor)
        } else {
            canvas.drawLine(startX, startY, startX, stopY, backgroundBarPrimeColor)
        }
    }

    private fun getBaseLine() = height / 2 - (if (drawTimeCodes) 12.px else 0)
    private fun getStartBar() = max(0, getBarPosition().toInt() - (maxVisibleBars * drawStartPosition).toInt())
    private fun getEndBar() = min(amps.size, getStartBar() + maxVisibleBars)
    private fun getBarHeightAt(i: Int) = (height - (if (drawTimeCodes) 40.px else 0)) * max(0.01f, min(amps[i] / maxAmp, 0.9f))
    private fun getBarPosition() = cursorPosition / tickPerBar.toFloat()
    private fun inRangePosition(position: Float) = min(tickCount.toFloat(), max(0f, position))
    protected fun getTimeStamp(position: Float) = (position.toLong() * tickDuration)
    protected fun calculateCursorPosition(currentTime: Long) =
        inRangePosition(currentTime / tickDuration.toFloat())

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        maxVisibleBars = (width / (barWidth + spaceBetweenBar)).toInt()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        maxVisibleBars = (w / (barWidth + spaceBetweenBar)).toInt()
    }

    override fun onDetachedFromWindow() {
        ampNormalizer = { 0 }
        super.onDetachedFromWindow()
    }
}