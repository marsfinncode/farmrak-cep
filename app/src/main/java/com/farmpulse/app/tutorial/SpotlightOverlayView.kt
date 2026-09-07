package com.farmpulse.app.tutorial

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator

/**
 * วาดพื้นหลังมืดครึ่งโปร่งใสเต็มจอ พร้อม "รูวงกลม" ที่ตำแหน่งของ target ปัจจุบัน (spotlight)
 * เทคนิคมาตรฐาน: วาดลง Bitmap แยก แล้วเจาะรูด้วย PorterDuff.Mode.CLEAR ก่อนนำไปวาดทับหน้าจอจริง
 * ต้องบังคับ software layer เพราะ CLEAR mode ทำงานไม่เสถียรบน hardware-accelerated canvas
 */
class SpotlightOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var spotlightCx = -1f
    private var spotlightCy = -1f
    private var spotlightRadius = 0f
    private var animator: ValueAnimator? = null

    private var bitmap: Bitmap? = null
    private var bitmapCanvas: Canvas? = null

    private val overlayPaint = Paint().apply { color = Color.parseColor("#CC1A1A14") }
    private val clearPaint = Paint().apply {
        isAntiAlias = true
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }
    private val ringPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 5f
        color = Color.WHITE
        alpha = 200
    }

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            bitmapCanvas = Canvas(bitmap!!)
        }
    }

    /** เคลื่อน spotlight ไปยังตำแหน่ง/ขนาดใหม่แบบเนียนๆ (เล่นครั้งเดียวจบ ไม่วนซ้ำ) */
    fun animateTo(targetRect: RectF, paddingPx: Float = 20f) {
        val newCx = targetRect.centerX()
        val newCy = targetRect.centerY()
        val newRadius = (maxOf(targetRect.width(), targetRect.height()) / 2f) + paddingPx

        animator?.cancel()
        if (spotlightCx < 0f) {
            // ตำแหน่งแรก ไม่ต้อง animate การเคลื่อนที่ แค่ขยายวงออกมาเบาๆ
            spotlightCx = newCx
            spotlightCy = newCy
            animator = ValueAnimator.ofFloat(0f, newRadius).apply {
                duration = 260
                interpolator = DecelerateInterpolator()
                addUpdateListener {
                    spotlightRadius = it.animatedValue as Float
                    invalidate()
                }
                start()
            }
        } else {
            val startCx = spotlightCx
            val startCy = spotlightCy
            val startRadius = spotlightRadius
            animator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 320
                interpolator = DecelerateInterpolator()
                addUpdateListener {
                    val t = it.animatedValue as Float
                    spotlightCx = startCx + (newCx - startCx) * t
                    spotlightCy = startCy + (newCy - startCy) * t
                    spotlightRadius = startRadius + (newRadius - startRadius) * t
                    invalidate()
                }
                start()
            }
        }
    }

    /** ซ่อน spotlight ไปเลย (เช่น ตอน step แนะนำตัวที่ยังไม่มี target เจาะจง) */
    fun clearSpotlight() {
        animator?.cancel()
        spotlightCx = -1f
        spotlightRadius = 0f
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val bmp = bitmap ?: return
        val bCanvas = bitmapCanvas ?: return

        bCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        bCanvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)

        if (spotlightCx >= 0f && spotlightRadius > 0f) {
            bCanvas.drawCircle(spotlightCx, spotlightCy, spotlightRadius, clearPaint)
        }

        canvas.drawBitmap(bmp, 0f, 0f, null)

        if (spotlightCx >= 0f && spotlightRadius > 0f) {
            canvas.drawCircle(spotlightCx, spotlightCy, spotlightRadius, ringPaint)
        }
    }
}
