package com.farmpulse.app.util

import android.view.animation.Interpolator
import android.view.animation.PathInterpolator

/**
 * ค่ามาตรฐานของ animation ทั้งแอป — รวมไว้ที่เดียวเพื่อให้ทุกหน้าจอเคลื่อนไหวด้วยจังหวะเดียวกัน
 *
 * หลักการที่ยึด (สำคัญกับมือถือเกษตรกรที่อาจไม่แรง):
 *  - ใช้เฉพาะ alpha / translation / scale ซึ่ง GPU จัดการได้เอง ไม่กินซีพียู
 *  - ไม่มี animation ที่วนซ้ำตลอดเวลา ทุกอันเล่นครั้งเดียวจบ
 *  - ระยะเวลาสั้น (140-260ms) ให้รู้สึกลื่นไหลแต่ไม่หน่วงเวลาใช้งาน
 */
object AnimUtils {

    const val DURATION_SHORT = 140L
    const val DURATION_MEDIUM = 220L
    const val DURATION_LONG = 260L

    /**
     * เส้นโค้งความเร็วแบบ "emphasized" — ออกตัวเร็วแล้วค่อยๆ ชะลอลงนุ่มๆ ตอนจบ
     * ให้ความรู้สึกเป็นธรรมชาติกว่า interpolator เริ่มต้นของ Android ที่ค่อนข้างแข็ง
     */
    fun emphasized(): Interpolator = PathInterpolator(0.2f, 0f, 0f, 1f)

    /** สำหรับสิ่งที่กำลังจะหายไป — เร่งออกไปเลย ไม่ต้องหน่วง */
    fun exit(): Interpolator = PathInterpolator(0.4f, 0f, 1f, 1f)
}
