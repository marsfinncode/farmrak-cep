package com.farmpulse.app.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import com.farmpulse.app.data.HistoryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** วาดกราฟเส้นย้อนหลัง (ความชื้น หรือ pH) พร้อมป้ายเวลากำกับด้านล่าง ให้ดูง่ายว่าช่วงไหนกราฟขึ้น/ลง */
class SparklineView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var points: List<HistoryPoint> = emptyList()
    var lineColor: Int = Color.BLUE

    private val density = resources.displayMetrics.density
    private val labelAreaHeight = 18f * density

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#8A8578") // ink_soft โดยประมาณ (ไม่พึ่ง context resource เพื่อไม่ผูก theme)
        textSize = 10.5f * density
        textAlign = Paint.Align.CENTER
    }

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    /** ใช้ตัวนี้แทน setValues เดิม — รับจุดข้อมูลพร้อมเวลา ไม่ใช่แค่ตัวเลขเฉยๆ */
    fun setPoints(newPoints: List<HistoryPoint>) {
        points = newPoints
        invalidate()
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (points.isEmpty()) return

        val graphHeight = height - labelAreaHeight

        // มีข้อมูลแค่จุดเดียว (เพิ่งเชื่อมต่อ ยังไม่ทันมีจุดที่สอง) — วาดเป็นจุดกลมไว้ก่อน
        // ดีกว่าปล่อยว่างเปล่าเฉยๆ จนดูเหมือนกราฟพัง ทั้งที่จริงๆ มีข้อมูลอยู่แล้ว
        if (points.size == 1) {
            dotPaint.color = lineColor
            canvas.drawCircle(width / 2f, graphHeight / 2f, 7f, dotPaint)
            drawTimeLabels(canvas, 0f)
            return
        }
        val values = points.map { it.value }
        val min = values.min()
        val max = values.max()
        val range = (max - min).takeIf { it > 0.01f } ?: 1f

        val stepX = width / (points.size - 1).toFloat()
        val path = Path()
        points.forEachIndexed { index, p ->
            val x = index * stepX
            val normalized = (p.value - min) / range
            val y = graphHeight - (normalized * graphHeight)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        linePaint.color = lineColor
        canvas.drawPath(path, linePaint)

        drawTimeLabels(canvas, stepX)
    }

    /** วางป้ายเวลา 3 จุด: เริ่มต้น-กลาง-ล่าสุด ให้พอเห็นว่าช่วงเวลาไหนกราฟขึ้น/ลง โดยไม่รกเกินไป */
    private fun drawTimeLabels(canvas: Canvas, stepX: Float) {
        val labelY = height - (labelAreaHeight * 0.15f)
        val indices = listOf(0, points.size / 2, points.size - 1).distinct()

        indices.forEach { index ->
            val label = timeFormat.format(Date(points[index].timestampMillis))
            val halfTextWidth = labelPaint.measureText(label) / 2
            val minX = halfTextWidth
            val maxX = (width - halfTextWidth).coerceAtLeast(minX) // กันกรณีจอแคบผิดปกติจนช่วงกลับด้าน
            val x = (index * stepX).coerceIn(minX, maxX)
            canvas.drawText(label, x, labelY, labelPaint)
        }
    }
}
