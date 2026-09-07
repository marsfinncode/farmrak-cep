package com.farmpulse.app.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.farmpulse.app.R

/**
 * แสดงระดับความชื้นในดินเป็นแท่งเติมน้ำ (เหมือนภาพตัดขวางของดินที่มีน้ำอยู่ด้านล่าง)
 * ค่าที่ป้อนเข้ามาคือ "ตัวแปรจาก ESP32" (soil_moisture) ในหน่วยเปอร์เซ็นต์ 0-100
 */
class SoilMoistureView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var displayedValue = 0f
    private var targetValue = 0f
    private var animator: ValueAnimator? = null

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.soil_dark)
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cornerRadius = 40f

    fun setMoisture(percent: Float, animate: Boolean = true) {
        targetValue = percent.coerceIn(0f, 100f)
        if (!animate) {
            displayedValue = targetValue
            invalidate()
            return
        }
        animator?.cancel()
        animator = ValueAnimator.ofFloat(displayedValue, targetValue).apply {
            duration = 900
            addUpdateListener {
                displayedValue = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()

        val bgRect = RectF(0f, 0f, w, h)
        canvas.drawRoundRect(bgRect, cornerRadius, cornerRadius, bgPaint)

        val fillHeight = h * (displayedValue / 100f)
        if (fillHeight > 0f) {
            val fillTop = h - fillHeight
            fillPaint.shader = LinearGradient(
                0f, fillTop, 0f, h,
                ContextCompat.getColor(context, R.color.water_light),
                ContextCompat.getColor(context, R.color.water),
                Shader.TileMode.CLAMP
            )
            val fillRect = RectF(0f, fillTop, w, h)
            canvas.save()
            canvas.clipRect(0f, 0f, w, h)
            canvas.drawRoundRect(RectF(0f, fillTop, w, h + cornerRadius), cornerRadius, cornerRadius, fillPaint)
            canvas.restore()
        }
    }
}
