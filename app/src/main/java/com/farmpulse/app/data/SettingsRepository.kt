package com.farmpulse.app.data

import android.content.Context
import android.content.SharedPreferences

/**
 * เก็บค่าตั้งค่าที่ผู้ใช้กำหนดเอง (IP บอร์ด, เกณฑ์ต่างๆ) ลงในเครื่อง
 * เพื่อให้แอปจำค่าไว้แม้ปิดแอปไปแล้ว และใช้ค่าเดิมตอนเชื่อมต่อบอร์ดครั้งถัดไป
 *
 * ไม่มี broker/username/password อีกต่อไป — เวอร์ชันนี้ต่อกับบอร์ดโดยตรงผ่าน WiFi ท้องถิ่น
 */
class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("farmpulse_settings", Context.MODE_PRIVATE)

    /** IP ของบอร์ด ESP32 ในโหมด Access Point — ค่าเริ่มต้นตรงกับ IP มาตรฐานของ ESP32 Soft-AP */
    var boardIpAddress: String
        get() = prefs.getString(KEY_BOARD_IP, DEFAULT_BOARD_IP) ?: DEFAULT_BOARD_IP
        set(value) = prefs.edit().putString(KEY_BOARD_IP, value).apply()

    var moistureThreshold: Int
        get() = prefs.getInt(KEY_MOISTURE_THRESHOLD, 30)
        set(value) = prefs.edit().putInt(KEY_MOISTURE_THRESHOLD, value).apply()

    var phMin: Float
        get() = prefs.getFloat(KEY_PH_MIN, 5.5f)
        set(value) = prefs.edit().putFloat(KEY_PH_MIN, value).apply()

    var phMax: Float
        get() = prefs.getFloat(KEY_PH_MAX, 7.5f)
        set(value) = prefs.edit().putFloat(KEY_PH_MAX, value).apply()

    /**
     * IP ของบอร์ดบนเครือข่าย WiFi บ้าน/ไร่ — แอป "เรียนรู้" ค่านี้เองอัตโนมัติหลังตั้งค่า WiFi ให้บอร์ดสำเร็จ
     * ผู้ใช้ไม่ต้องจดหรือพิมพ์เอง
     */
    var boardHomeIp: String
        get() = prefs.getString(KEY_BOARD_HOME_IP, "") ?: ""
        set(value) = prefs.edit().putString(KEY_BOARD_HOME_IP, value).apply()

    /** IP ล่าสุดที่เชื่อมต่อสำเร็จจริง — ลองอันนี้ก่อนเสมอ ทำให้เชื่อมต่อครั้งถัดไปเร็วขึ้นมาก */
    var lastWorkingIp: String
        get() = prefs.getString(KEY_LAST_WORKING_IP, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_WORKING_IP, value).apply()

    /**
     * รายชื่อ IP ที่จะไล่ลองเชื่อมต่อตามลำดับ — หัวใจของระบบค้นหาบอร์ดอัตโนมัติ
     * เรียงจาก "น่าจะติดเร็วที่สุด" ไป "ทางเลือกสุดท้าย" และตัดตัวซ้ำ/ค่าว่างออก
     */
    fun boardIpCandidates(): List<String> =
        listOf(lastWorkingIp, boardHomeIp, boardIpAddress, DEFAULT_BOARD_IP)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()

    /** id ของพืชที่เลือกเป็น preset ไว้ — ว่าง = "กำหนดเอง" (ผู้ใช้ปรับ slider เอง) */
    var plantPresetId: String
        get() = prefs.getString(KEY_PLANT_PRESET, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PLANT_PRESET, value).apply()

    companion object {
        private const val KEY_PLANT_PRESET = "plant_preset_id"
        private const val KEY_BOARD_HOME_IP = "board_home_ip"
        private const val KEY_LAST_WORKING_IP = "last_working_ip"
        private const val KEY_BOARD_IP = "board_ip_address"
        private const val KEY_MOISTURE_THRESHOLD = "moisture_threshold"
        private const val KEY_PH_MIN = "ph_min"
        private const val KEY_PH_MAX = "ph_max"

        // 192.168.4.1 คือ IP มาตรฐานที่ ESP32 ใช้เสมอเมื่อเปิดโหมด WiFi Access Point (Soft-AP)
        const val DEFAULT_BOARD_IP = "192.168.4.1"
    }
}
