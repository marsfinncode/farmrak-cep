package com.farmpulse.app.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.farmpulse.app.R

/** แสดงค่า pH (ตัวแปรจาก ESP32: ph_value) เป็นเข็มวัดครึ่งวงกลม ช่วง 0-14 */
class PhGaugeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var displayedPh = 7f
    private var animator: ValueAnimator? = null

    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 22f
        strokeCap = Paint.Cap.ROUND
    }
    private val needlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.paper)
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
    }

    fun setPhValue(value: Float, animate: Boolean = true) {
        val clamped = value.coerceIn(0f, 14f)
        if (!animate) {
            displayedPh = clamped
            invalidate()
            return
        }
        animator?.cancel()
        animator = ValueAnimator.ofFloat(displayedPh, clamped).apply {
            duration = 900
            addUpdateListener {
                displayedPh = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2f
        val cy = h * 0.92f
        val radius = (minOf(w, h * 1.6f) / 2f) - 20f
        val rect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)

        // โซนกรด (แดง) 0-5.5, โซนเหมาะสม (เขียว) 5.5-7.5, โซนด่าง (ส้ม) 7.5-14
        arcPaint.color = ContextCompat.getColor(context, R.color.rust)
        canvas.drawArc(rect, 180f, (5.5f / 14f) * 180f, false, arcPaint)

        arcPaint.color = ContextCompat.getColor(context, R.color.sprout)
        canvas.drawArc(rect, 180f + (5.5f / 14f) * 180f, (2f / 14f) * 180f, false, arcPaint)

        arcPaint.color = ContextCompat.getColor(context, R.color.amber)
        canvas.drawArc(rect, 180f + (7.5f / 14f) * 180f, (6.5f / 14f) * 180f, false, arcPaint)

        // เข็มชี้ตำแหน่งค่า pH ปัจจุบัน
        val angleDeg = 180f + (displayedPh / 14f) * 180f
        val angleRad = Math.toRadians(angleDeg.toDouble())
        val needleLen = radius - 10f
        val nx = cx + (needleLen * Math.cos(angleRad)).toFloat()
        val ny = cy + (needleLen * Math.sin(angleRad)).toFloat()
        canvas.drawLine(cx, cy, nx, ny, needlePaint)
        canvas.drawCircle(cx, cy, 9f, needlePaint)
    }
}
