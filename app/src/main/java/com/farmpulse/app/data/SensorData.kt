package com.farmpulse.app.data

import org.json.JSONObject

/**
 * โครงสร้างข้อมูลที่ ESP32 ส่งขึ้นมาผ่าน MQTT (topic: farmpulse/{device_id}/telemetry)
 * ตัวอย่าง JSON payload ที่ ESP32 ต้องส่ง:
 *
 * {
 *   "device_id": "esp32_garden_01",
 *   "soil_moisture": 45.2,
 *   "ph_value": 6.5,
 *   "pump_status": false,
 *   "fertilizer_status": false,
 *   "auto_mode": true,
 *   "signal_rssi": -58,
 *   "battery_pct": 86,
 *   "timestamp": 1721460600
 * }
 */
data class SensorData(
    val deviceId: String = "esp32_garden_01",
    val soilMoisture: Float = 0f,          // 0-100 %
    val phValue: Float = 7f,               // 0-14
    val pumpStatus: Boolean = false,
    val fertilizerStatus: Boolean = false,
    val autoMode: Boolean = true,
    val signalRssi: Int = 0,               // dBm ยิ่งใกล้ 0 ยิ่งแรง
    val batteryPct: Int = 100,
    val timestampMillis: Long = System.currentTimeMillis()
) {
    companion object {
        /** แปลง JSON payload จาก ESP32 ให้เป็น SensorData — จุดนี้คือที่ "ตัวแปรจาก ESP32" ถูกอ่านเข้าแอปจริงๆ */
        fun fromJson(json: String): SensorData {
            val o = JSONObject(json)
            return SensorData(
                deviceId = o.optString("device_id", "esp32_garden_01"),
                soilMoisture = o.optDouble("soil_moisture", 0.0).toFloat(),
                phValue = o.optDouble("ph_value", 7.0).toFloat(),
                pumpStatus = o.optBoolean("pump_status", false),
                fertilizerStatus = o.optBoolean("fertilizer_status", false),
                autoMode = o.optBoolean("auto_mode", true),
                signalRssi = o.optInt("signal_rssi", 0),
                batteryPct = o.optInt("battery_pct", 100),
                timestampMillis = o.optLong("timestamp", System.currentTimeMillis() / 1000) * 1000
            )
        }
    }

    fun phStatusLabel(min: Float, max: Float): PhStatus = when {
        phValue < min -> PhStatus.TOO_ACIDIC
        phValue > max -> PhStatus.TOO_ALKALINE
        else -> PhStatus.OK
    }
}

enum class PhStatus { OK, TOO_ACIDIC, TOO_ALKALINE }

/** คำสั่งที่แอปส่ง "กลับไป" หา ESP32 ผ่าน topic: farmpulse/{device_id}/command */
data class DeviceCommand(
    val setAutoMode: Boolean? = null,
    val setPump: Boolean? = null,
    val setFertilizer: Boolean? = null,
    val moistureThreshold: Int? = null,
    val phMin: Float? = null,
    val phMax: Float? = null
) {
    fun toJson(): String {
        val o = JSONObject()
        setAutoMode?.let { o.put("auto_mode", it) }
        setPump?.let { o.put("pump", it) }
        setFertilizer?.let { o.put("fertilizer", it) }
        moistureThreshold?.let { o.put("moisture_threshold", it) }
        phMin?.let { o.put("ph_min", it) }
        phMax?.let { o.put("ph_max", it) }
        return o.toString()
    }
}
