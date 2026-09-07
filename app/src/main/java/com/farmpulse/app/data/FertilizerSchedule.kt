package com.farmpulse.app.data

/** ช่วงเวลาให้ปุ๋ยอัตโนมัติ 1 ช่วง — เริ่มที่ hour:minute นาน durationMinutes นาที */
data class FertilizerSchedule(
    val id: String,
    val hour: Int,          // 0-23
    val minute: Int,        // 0-59
    val durationMinutes: Int
) {
    /** true ถ้าเวลาปัจจุบัน (นาทีนับจากเที่ยงคืน) อยู่ในช่วงที่ต้องให้ปุ๋ย */
    fun isActiveAt(nowMinutesOfDay: Int): Boolean {
        val startMinutes = hour * 60 + minute
        val endMinutes = startMinutes + durationMinutes
        return if (endMinutes <= 24 * 60) {
            nowMinutesOfDay in startMinutes until endMinutes
        } else {
            // ช่วงเวลาข้ามเที่ยงคืน (เช่น เริ่ม 23:50 นาน 20 นาที ไปจบ 00:10)
            nowMinutesOfDay >= startMinutes || nowMinutesOfDay < (endMinutes - 24 * 60)
        }
    }

    fun displayTime(): String = String.format("%02d:%02d", hour, minute)
}
