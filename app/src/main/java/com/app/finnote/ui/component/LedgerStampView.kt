package com.app.finnote.ui.component

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.app.finnote.R
import kotlin.math.min

class LedgerStampView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val circleColor = ContextCompat.getColor(context, R.color.income_tint)
    private val checkColor = ContextCompat.getColor(context, R.color.action_green_accessible)

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = circleColor
    }
    private val checkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = checkColor
    }

    private val checkPath = Path()
    private val checkSegment = Path()
    private val pathMeasure = PathMeasure()
    private var stampProgress = 1f

    fun setStampProgress(progress: Float) {
        stampProgress = progress.coerceIn(0f, 1f)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val size = min(width, height).toFloat()
        if (size <= 0f) return

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = size * 0.42f
        val circleProgress = easeOutQuint((stampProgress / 0.46f).coerceIn(0f, 1f))
        val checkProgress = easeOutQuint(((stampProgress - 0.22f) / 0.58f).coerceIn(0f, 1f))
        val circleScale = 0.84f + (0.16f * circleProgress)

        circlePaint.alpha = (255 * circleProgress).toInt().coerceIn(0, 255)
        canvas.drawCircle(centerX, centerY, radius * circleScale, circlePaint)

        drawCheck(canvas, centerX, centerY, size, checkProgress)
    }

    private fun drawCheck(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        size: Float,
        progress: Float
    ) {
        if (progress <= 0f) return

        checkPath.reset()
        checkPath.moveTo(centerX - (size * 0.16f), centerY - (size * 0.01f))
        checkPath.lineTo(centerX - (size * 0.04f), centerY + (size * 0.12f))
        checkPath.lineTo(centerX + (size * 0.2f), centerY - (size * 0.14f))

        pathMeasure.setPath(checkPath, false)
        checkSegment.reset()
        pathMeasure.getSegment(0f, pathMeasure.length * progress, checkSegment, true)

        checkPaint.strokeWidth = 4.4f.dp()
        checkPaint.alpha = (255 * progress).toInt().coerceIn(0, 255)
        canvas.drawPath(checkSegment, checkPaint)
    }

    private fun easeOutQuint(value: Float): Float {
        val inverse = 1f - value.coerceIn(0f, 1f)
        return 1f - (inverse * inverse * inverse * inverse * inverse)
    }

    private fun Float.dp(): Float {
        return this * resources.displayMetrics.density
    }
}
