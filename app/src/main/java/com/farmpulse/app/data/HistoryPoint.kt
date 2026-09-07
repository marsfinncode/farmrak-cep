package com.farmpulse.app.data

/** จุดข้อมูลย้อนหลัง 1 จุด พร้อมเวลาที่บันทึก — ใช้กับกราฟความชื้น/pH ในหน้าประวัติ */
data class HistoryPoint(
    val value: Float,
    val timestampMillis: Long
)
