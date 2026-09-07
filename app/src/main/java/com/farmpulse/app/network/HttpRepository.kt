package com.farmpulse.app.network

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.farmpulse.app.data.DeviceCommand
import com.farmpulse.app.data.FertilizerSchedule
import com.farmpulse.app.data.SensorData
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

enum class ConnectionState { DISCONNECTED, CONNECTING, CONNECTED, RECONNECTING, ERROR }

/**
 * คุยกับ ESP32 ผ่าน HTTP ธรรมดาในเครือข่ายท้องถิ่นเท่านั้น — ไม่มี broker, ไม่มีอินเทอร์เน็ต
 *
 * ===== ระบบค้นหาบอร์ดอัตโนมัติ =====
 * บอร์ดมี IP ได้ 2 แบบ ขึ้นกับว่ามือถือต่อ WiFi อะไรอยู่:
 *   - ต่อ WiFi ของบอร์ดโดยตรง  -> บอร์ดอยู่ที่ 192.168.4.1 เสมอ
 *   - ต่อ WiFi บ้าน/ไร่เดียวกัน -> บอร์ดได้ IP จากเราเตอร์ (เปลี่ยนได้เรื่อยๆ ตาม DHCP)
 *
 * แทนที่จะให้เกษตรกรมานั่งจำ/พิมพ์เลข IP เองแล้วต้องแก้ทุกครั้งที่ย้ายเครือข่ายหรือเราเตอร์รีบูต
 * แอปจะ "ไล่ลองทีละ IP" ให้เองอัตโนมัติ แล้วจำอันที่ใช้ได้ไว้ลองก่อนในครั้งถัดไป
 */
class HttpRepository(
    /** ดึงรายชื่อ IP ที่จะไล่ลอง (อ่านสดทุกครั้ง เพราะค่าอาจเปลี่ยนระหว่างใช้งาน) */
    private val candidateProvider: () -> List<String>,
    /** แจ้งกลับเมื่อเจอ IP ที่ใช้ได้จริง เพื่อให้ฝั่งเรียกใช้บันทึกไว้ */
    private val onWorkingIpFound: (String) -> Unit = {},
    /** แจ้งกลับเมื่อเรียนรู้ IP ของบอร์ดบนเครือข่ายบ้าน (จาก /api/wifi_status) */
    private val onHomeIpLearned: (String) -> Unit = {}
) {

    private val _sensorData = MutableLiveData<SensorData>()
    val sensorData: LiveData<SensorData> = _sensorData

    private val _connectionState = MutableLiveData(ConnectionState.DISCONNECTED)
    val connectionState: LiveData<ConnectionState> = _connectionState

    /** IP ที่กำลังใช้เชื่อมต่ออยู่จริง — ให้ UI เอาไปแสดงได้ว่าตอนนี้คุยกับบอร์ดผ่านทางไหน */
    private val _activeIp = MutableLiveData<String?>(null)
    val activeIp: LiveData<String?> = _activeIp

    private val pollHandler = Handler(Looper.getMainLooper())
    private var pollRunnable: Runnable? = null

    @Volatile private var currentIp: String? = null
    @Volatile private var consecutiveFailures = 0

    fun startPolling(intervalMs: Long = 4000) {
        stopPolling()
        _connectionState.postValue(ConnectionState.CONNECTING)

        val runnable = object : Runnable {
            override fun run() {
                fetchStatus()
                pollHandler.postDelayed(this, intervalMs)
            }
        }
        pollRunnable = runnable
        pollHandler.post(runnable)
    }

    fun stopPolling() {
        pollRunnable?.let { pollHandler.removeCallbacks(it) }
        pollRunnable = null
        currentIp = null
        _activeIp.postValue(null)
        _connectionState.postValue(ConnectionState.DISCONNECTED)
    }

    fun pausePolling() {
        pollRunnable?.let { pollHandler.removeCallbacks(it) }
    }

    fun resumePolling() {
        pollRunnable?.let {
            pollHandler.removeCallbacks(it)
            pollHandler.post(it)
        }
    }

    /** บังคับให้ลืม IP เดิมแล้วค้นหาใหม่ (เช่น ผู้ใช้เพิ่งกรอก IP เองหรือเพิ่งย้าย WiFi) */
    fun forgetAndRediscover() {
        currentIp = null
        consecutiveFailures = 0
        _activeIp.postValue(null)
        _connectionState.postValue(ConnectionState.CONNECTING)
    }

    private fun fetchStatus() {
        Thread {
            val wasConnected = _connectionState.value == ConnectionState.CONNECTED

            // ถ้ายังไม่รู้ว่าบอร์ดอยู่ IP ไหน ให้ไล่ค้นหาก่อน
            var ip = currentIp
            if (ip == null) {
                ip = discoverBoard()
                if (ip == null) {
                    _connectionState.postValue(ConnectionState.ERROR)
                    return@Thread
                }
                currentIp = ip
                _activeIp.postValue(ip)
                onWorkingIpFound(ip)
            }

            val body = requestStatus(ip, READ_TIMEOUT_MS)
            if (body == null) {
                consecutiveFailures++
                // พลาดติดกันหลายครั้ง = น่าจะย้ายเครือข่าย/บอร์ดเปลี่ยน IP -> ลืมแล้วค้นหาใหม่รอบหน้า
                if (consecutiveFailures >= MAX_FAILURES_BEFORE_REDISCOVER) {
                    currentIp = null
                    _activeIp.postValue(null)
                    consecutiveFailures = 0
                    _connectionState.postValue(ConnectionState.ERROR)
                } else {
                    _connectionState.postValue(ConnectionState.RECONNECTING)
                }
                return@Thread
            }

            consecutiveFailures = 0
            try {
                _sensorData.postValue(SensorData.fromJson(body))
                _connectionState.postValue(ConnectionState.CONNECTED)
                if (!wasConnected) {
                    // เพิ่งเชื่อมต่อสำเร็จรอบใหม่ — ตั้งเวลาบอร์ดให้ตรงมือถือ และเรียนรู้ IP บ้านของบอร์ดไว้
                    syncTime()
                    learnHomeIp()
                }
            } catch (e: Exception) {
                Log.e(TAG, "อ่านข้อมูลจากบอร์ดไม่สำเร็จ: " + e.message)
                _connectionState.postValue(ConnectionState.ERROR)
            }
        }.start()
    }

    /** ไล่ลองทีละ IP จนกว่าจะเจอตัวที่ตอบกลับจริง */
    private fun discoverBoard(): String? {
        for (candidate in candidateProvider()) {
            if (requestStatus(candidate, PROBE_TIMEOUT_MS) != null) {
                Log.i(TAG, "เจอบอร์ดที่ " + candidate)
                return candidate
            }
        }
        return null
    }

    private fun requestStatus(ip: String, timeoutMs: Int): String? {
        return try {
            val conn = URL("http://" + ip + "/api/status").openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = timeoutMs
            conn.readTimeout = timeoutMs
            conn.setRequestProperty("Accept", "application/json, text/plain, */*")
            conn.setRequestProperty("Connection", "close")
            val body = if (conn.responseCode == 200)
                conn.inputStream.bufferedReader().use { it.readText() } else null
            conn.disconnect()
            body
        } catch (e: Exception) {
            null
        }
    }

    /** ถาม IP ของบอร์ดบนเครือข่ายบ้าน แล้วจำไว้ใช้ตอนมือถือกลับมาต่อ WiFi บ้าน */
    private fun learnHomeIp() {
        fetchWifiStatus { status ->
            if (status != null && status.staConnected && status.staIp.isNotBlank()) {
                onHomeIpLearned(status.staIp)
            }
        }
    }

    fun sendCommand(command: DeviceCommand, onResult: (Boolean) -> Unit = {}) {
        postJson("/api/command", command.toJson(), onResult)
    }

    fun syncTime(onResult: (Boolean) -> Unit = {}) {
        val unixSeconds = System.currentTimeMillis() / 1000
        val tzOffsetMinutes = java.util.TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 60000
        postJson("/api/set_time", "{\"unix_time\":" + unixSeconds + ",\"tz_offset_minutes\":" + tzOffsetMinutes + "}", onResult)
    }

    fun sendFertilizerSchedule(schedules: List<FertilizerSchedule>, onResult: (Boolean) -> Unit = {}) {
        val items = schedules.joinToString(",") {
            "{\"hour\":" + it.hour + ",\"minute\":" + it.minute + ",\"duration\":" + it.durationMinutes + "}"
        }
        postJson("/api/fert_schedule", "{\"schedules\":[" + items + "]}", onResult)
    }

    fun sendWifiConfig(ssid: String, password: String, onResult: (Boolean) -> Unit = {}) {
        val escapedSsid = ssid.replace("\\", "\\\\").replace("\"", "\\\"")
        val escapedPassword = password.replace("\\", "\\\\").replace("\"", "\\\"")
        postJson("/api/wifi_config", "{\"ssid\":\"" + escapedSsid + "\",\"password\":\"" + escapedPassword + "\"}", onResult)
    }

    fun fetchWifiStatus(onResult: (WifiStatus?) -> Unit) {
        Thread {
            val ip = currentIp ?: discoverBoard()
            if (ip == null) {
                onResult(null)
                return@Thread
            }
            try {
                val conn = URL("http://" + ip + "/api/wifi_status").openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = READ_TIMEOUT_MS
                conn.readTimeout = READ_TIMEOUT_MS
                conn.setRequestProperty("Accept", "application/json, text/plain, */*")
                conn.setRequestProperty("Connection", "close")
                val result = if (conn.responseCode == 200)
                    WifiStatus.fromJson(conn.inputStream.bufferedReader().use { it.readText() }) else null
                conn.disconnect()
                pollHandler.post { onResult(result) }
            } catch (e: Exception) {
                pollHandler.post { onResult(null) }
            }
        }.start()
    }

    private fun postJson(path: String, jsonBody: String, onResult: (Boolean) -> Unit) {
        Thread {
            var ip = currentIp
            if (ip == null) {
                ip = discoverBoard()
                if (ip != null) {
                    currentIp = ip
                    _activeIp.postValue(ip)
                    onWorkingIpFound(ip)
                }
            }
            if (ip == null) {
                onResult(false)
                return@Thread
            }

            val success: Boolean = try {
                val conn = URL("http://" + ip + path).openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.connectTimeout = COMMAND_TIMEOUT_MS
                conn.readTimeout = COMMAND_TIMEOUT_MS
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                conn.setRequestProperty("Accept", "application/json, text/plain, */*")
                conn.setRequestProperty("Connection", "close")

                val bytes = jsonBody.toByteArray(Charsets.UTF_8)
                conn.setFixedLengthStreamingMode(bytes.size)
                conn.outputStream.use { os ->
                    os.write(bytes)
                    os.flush()
                }

                val isOk = conn.responseCode in 200..299

                // อ่านและเคลียร์ stream เพื่อให้การสื่อสาร HTTP สิ้นสุดอย่างสมบูรณ์
                try {
                    if (isOk) {
                        conn.inputStream?.bufferedReader()?.use { it.readText() }
                    } else {
                        conn.errorStream?.bufferedReader()?.use { it.readText() }
                    }
                } catch (ignored: Exception) {
                }

                conn.disconnect()
                isOk
            } catch (e: Exception) {
                Log.e(TAG, "ส่ง request ไป " + path + " ไม่สำเร็จ: " + e.message)
                false
            }

            pollHandler.post { onResult(success) }

            // ถ้าส่งคำสั่งสำเร็จ ให้ดึงสถานะเซนเซอร์ใหม่หลังผ่านไปเล็กน้อย เพื่ออัปเดต UI โดยไม่แย่ง socket กับ ESP32
            if (success) {
                pollHandler.postDelayed({ fetchStatus() }, 500)
            }
        }.start()
    }

    companion object {
        private const val TAG = "HttpRepository"
        private const val READ_TIMEOUT_MS = 4000
        private const val COMMAND_TIMEOUT_MS = 6000
        // ตอนไล่ค้นหาต้องใช้ timeout สั้นกว่า ไม่งั้นถ้ามีหลายตัวเลือกจะรอนานเกินไป
        private const val PROBE_TIMEOUT_MS = 1500
        private const val MAX_FAILURES_BEFORE_REDISCOVER = 3
    }
}

data class WifiStatus(
    val staConfigured: Boolean,
    val staSsid: String,
    val staConnected: Boolean,
    val staIp: String,
    val apIp: String
) {
    companion object {
        fun fromJson(json: String): WifiStatus {
            val o = org.json.JSONObject(json)
            return WifiStatus(
                staConfigured = o.optBoolean("sta_configured", false),
                staSsid = o.optString("sta_ssid", ""),
                staConnected = o.optBoolean("sta_connected", false),
                staIp = o.optString("sta_ip", ""),
                apIp = o.optString("ap_ip", "")
            )
        }
    }
}
