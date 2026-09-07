package com.farmpulse.app.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/** เก็บช่วงเวลาให้ปุ๋ยอัตโนมัติที่ผู้ใช้กำหนดเอง — เพิ่มได้ไม่จำกัดจำนวน */
class FertilizerScheduleRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("farmpulse_fert_schedule", Context.MODE_PRIVATE)

    fun getAll(): List<FertilizerSchedule> {
        val json = prefs.getString(KEY_SCHEDULES, null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                val o = array.getJSONObject(i)
                FertilizerSchedule(
                    id = o.getString("id"),
                    hour = o.getInt("hour"),
                    minute = o.getInt("minute"),
                    durationMinutes = o.getInt("durationMinutes")
                )
            }.sortedBy { it.hour * 60 + it.minute }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun add(schedule: FertilizerSchedule): Boolean {
        val current = getAll().toMutableList()
        current.add(schedule)
        return saveAll(current)
    }

    fun delete(id: String): Boolean {
        val current = getAll().toMutableList()
        current.removeAll { it.id == id }
        return saveAll(current)
    }

    fun newId(): String = "sched_${System.currentTimeMillis()}"

    private fun saveAll(schedules: List<FertilizerSchedule>): Boolean {
        val array = JSONArray()
        schedules.forEach { s ->
            val o = JSONObject()
            o.put("id", s.id)
            o.put("hour", s.hour)
            o.put("minute", s.minute)
            o.put("durationMinutes", s.durationMinutes)
            array.put(o)
        }
        return try {
            prefs.edit().putString(KEY_SCHEDULES, array.toString()).commit()
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        private const val KEY_SCHEDULES = "fertilizer_schedules_json"
    }
}
