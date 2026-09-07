package com.farmpulse.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.farmpulse.app.data.DeviceCommand
import com.farmpulse.app.data.HistoryPoint
import com.farmpulse.app.data.LogEntry
import com.farmpulse.app.data.LogKind
import com.farmpulse.app.data.SensorData
import com.farmpulse.app.data.SettingsRepository
import com.farmpulse.app.network.ConnectionState
import com.farmpulse.app.network.HttpRepository
import com.farmpulse.app.notification.NotificationHelper
import com.farmpulse.app.util.Event

class SensorViewModel(
    private val settings: SettingsRepository,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    // ส่ง lambda ให้ repository ไปดึงรายชื่อ IP และรายงานผลกลับมาบันทึก
    // ทำให้ระบบค้นหาบอร์ดอัตโนมัติ "เรียนรู้" ที่อยู่บอร์ดได้เองโดยผู้ใช้ไม่ต้องทำอะไร
    private val httpRepository = HttpRepository(
        candidateProvider = { settings.boardIpCandidates() },
        onWorkingIpFound = { ip -> settings.lastWorkingIp = ip },
        onHomeIpLearned = { ip -> settings.boardHomeIp = ip }
    )

    val sensorData: LiveData<SensorData> = httpRepository.sensorData
    val connectionState: LiveData<ConnectionState> = httpRepository.connectionState

    // เก็บค่าความชื้นย้อนหลังไว้วาดกราฟ sparkline หน้าประวัติ (สูงสุด 30 จุดล่าสุด)
    private val _moistureHistory = MutableLiveData<List<HistoryPoint>>(emptyList())
    val moistureHistory: LiveData<List<HistoryPoint>> = _moistureHistory

    private val _phHistory = MutableLiveData<List<HistoryPoint>>(emptyList())
    val phHistory: LiveData<List<HistoryPoint>> = _phHistory

    // สุ่มเก็บจุดกราฟทุก 5 นาที (ไม่ใช่ทุกครั้งที่ poll) — ไม่งั้นกราฟจะเห็นแค่ ~2 นาทีล่าสุดเท่านั้น
    // เพราะ poll ถี่ทุก 4 วินาที การเก็บทุกจุดจะทำให้ 30 จุดหมดไปในเวลาไม่ถึง 2 นาที
    private var lastHistorySampleMs = 0L

    private val _logs = MutableLiveData<List<LogEntry>>(emptyList())
    val logs: LiveData<List<LogEntry>> = _logs

    // ผลการส่งคำสั่งรดน้ำ/ให้ปุ๋ย — ห่อด้วย Event เพื่อไม่ให้แสดงซ้ำตอนสลับแท็บกลับมา
    private val _commandFeedback = MutableLiveData<Event<CommandFeedback>>()
    val commandFeedback: LiveData<Event<CommandFeedback>> = _commandFeedback

    // ตัวแปรจำสถานะไว้เทียบ เพื่อแจ้งเตือนเฉพาะตอน "เปลี่ยนสถานะ" เท่านั้น ไม่ใช่ทุกครั้งที่ poll (กันแจ้งเตือนถี่ยิบ)
    private var wasConnected = false
    private var phAlertActive = false
    private var moistureAlertActive = false

    // ตัวแปรจำสถานะสำหรับ "บันทึก log" โดยเฉพาะ (แยกจากของ notification เพราะเงื่อนไขต่างกัน)
    private var lastPumpOn = false
    private var lastFertOn = false
    private var lastPhOutOfRange = false

    init {
        sensorData.observeForever { data ->
            appendHistory(data)
            checkAlertNotifications(data)
        }
        connectionState.observeForever { state ->
            checkConnectionNotification(state)
        }
    }

    /** เริ่มถามค่าจากบอร์ดซ้ำๆ (ต้องต่อ WiFi ของบอร์ดไว้ก่อนแล้วที่ระบบ ไม่ใช่ในแอปนี้) */
    fun connectToBoard() {
        httpRepository.startPolling()
    }

    /** ผู้ใช้กรอก IP เองจากหน้าตั้งค่า (ทางเลือกขั้นสูง — ปกติไม่ต้องกรอกเพราะแอปค้นหาเอง) */
    fun updateBoardIp(ipAddress: String) {
        settings.boardIpAddress = ipAddress
        settings.lastWorkingIp = ""   // ล้างของเดิม เพื่อให้ค่าที่เพิ่งกรอกได้ถูกลองด้วย
        httpRepository.forgetAndRediscover()
        connectToBoard()
    }

    /** IP ที่กำลังคุยกับบอร์ดอยู่จริง ณ ตอนนี้ (null = ยังหาไม่เจอ) */
    val activeBoardIp = httpRepository.activeIp

    /** เรียกตอนแอปสลับไปเบื้องหลัง (Activity.onStop) — ให้ BoardMonitorService รับช่วง poll ต่อแทน
     *  ป้องกันไม่ให้ poll ซ้ำซ้อนสองทางพร้อมกันจนเปลืองแบตเตอรี่/เน็ตโดยไม่จำเป็น */
    fun pausePolling() {
        httpRepository.pausePolling()
    }

    /** เรียกตอนแอปกลับมาอยู่หน้าจอ (Activity.onStart) */
    fun resumePolling() {
        httpRepository.resumePolling()
    }

    fun toggleAutoMode(enabled: Boolean) {
        httpRepository.sendCommand(DeviceCommand(setAutoMode = enabled))
    }

    fun setPump(on: Boolean) {
        httpRepository.sendCommand(DeviceCommand(setPump = on)) { success ->
            _commandFeedback.postValue(Event(CommandFeedback(CommandAction.WATER, success)))
            if (success) {
                addLog(LogKind.WATER, if (on) "สั่งรดน้ำด้วยตนเอง" else "สั่งหยุดรดน้ำด้วยตนเอง")
            }
        }
    }

    fun setFertilizer(on: Boolean) {
        httpRepository.sendCommand(DeviceCommand(setFertilizer = on)) { success ->
            _commandFeedback.postValue(Event(CommandFeedback(CommandAction.FERTILIZER, success)))
            if (success) {
                addLog(LogKind.FERTILIZER, if (on) "สั่งให้ปุ๋ยด้วยตนเอง" else "สั่งหยุดให้ปุ๋ยด้วยตนเอง")
            }
        }
    }

    fun updateThresholds(moisture: Int, phMin: Float, phMax: Float) {
        settings.moistureThreshold = moisture
        settings.phMin = phMin
        settings.phMax = phMax
        httpRepository.sendCommand(
            DeviceCommand(moistureThreshold = moisture, phMin = phMin, phMax = phMax)
        )
    }

    fun currentSettings() = settings

    /** ตั้งค่า WiFi บ้าน/ไร่ให้บอร์ด — onResult บอกผลจริงว่าส่งถึงบอร์ดสำเร็จหรือไม่ */
    fun sendBoardWifiConfig(ssid: String, password: String, onResult: (Boolean) -> Unit) {
        httpRepository.sendWifiConfig(ssid, password, onResult)
    }

    /** เช็คสถานะ WiFi บ้าน/ไร่ปัจจุบันของบอร์ด (เชื่อมต่อสำเร็จหรือยัง, IP อะไร) */
    fun fetchBoardWifiStatus(onResult: (com.farmpulse.app.network.WifiStatus?) -> Unit) {
        httpRepository.fetchWifiStatus(onResult)
    }

    /** ส่งเวลาปัจจุบันของมือถือไปให้บอร์ด sync ทันที (เรียกเองตอนกดปุ่ม "ซิงค์เวลา") */
    fun syncBoardTime(onResult: (Boolean) -> Unit = {}) {
        httpRepository.syncTime(onResult)
    }

    /** ส่งตารางให้ปุ๋ยไปเก็บที่บอร์ด เพื่อให้บอร์ดให้ปุ๋ยตามเวลาได้เองแม้ไม่มีมือถือเชื่อมต่ออยู่ */
    fun sendFertilizerSchedule(
        schedules: List<com.farmpulse.app.data.FertilizerSchedule>,
        onResult: (Boolean) -> Unit
    ) {
        httpRepository.sendFertilizerSchedule(schedules, onResult)
    }

    private fun appendHistory(data: SensorData) {
        val now = System.currentTimeMillis()
        val currentPointCount = _moistureHistory.value?.size ?: 0
        val sampleInterval = if (currentPointCount < WARMUP_POINT_COUNT)
            WARMUP_SAMPLE_INTERVAL_MS else HISTORY_SAMPLE_INTERVAL_MS

        if (now - lastHistorySampleMs >= sampleInterval) {
            lastHistorySampleMs = now
            val currentMoisture = _moistureHistory.value ?: emptyList()
            val currentPh = _phHistory.value ?: emptyList()
            _moistureHistory.postValue((currentMoisture + HistoryPoint(data.soilMoisture, now)).takeLast(HISTORY_MAX_POINTS))
            _phHistory.postValue((currentPh + HistoryPoint(data.phValue, now)).takeLast(HISTORY_MAX_POINTS))
        }

        // ===== บันทึก log เฉพาะตอน "เปลี่ยนสถานะ" เท่านั้น (edge-triggered) =====
        // ปั๊มน้ำ: บันทึกทั้งตอนเริ่มและตอนหยุด แยกว่าเป็นระบบอัตโนมัติหรือสั่งเอง
        if (data.pumpStatus != lastPumpOn) {
            if (data.pumpStatus) {
                val reason = if (data.autoMode)
                    "เริ่มรดน้ำอัตโนมัติ — ความชื้น ${data.soilMoisture.toInt()}% ต่ำกว่าเกณฑ์ ${settings.moistureThreshold}%"
                else
                    "เริ่มรดน้ำ (สั่งด้วยตนเอง)"
                addLog(LogKind.WATER, reason)
            } else {
                addLog(LogKind.WATER, "หยุดรดน้ำแล้ว — ความชื้นขึ้นมาที่ ${data.soilMoisture.toInt()}%")
            }
            lastPumpOn = data.pumpStatus
        }

        // ระบบให้ปุ๋ย: ตามตารางเวลาที่ตั้งไว้ (ไม่ผูกกับค่า pH แล้ว)
        if (data.fertilizerStatus != lastFertOn) {
            if (data.fertilizerStatus) {
                addLog(LogKind.FERTILIZER, if (data.autoMode) "เริ่มให้ปุ๋ยตามตารางเวลา" else "เริ่มให้ปุ๋ย (สั่งด้วยตนเอง)")
            } else {
                addLog(LogKind.FERTILIZER, "ให้ปุ๋ยเสร็จแล้ว")
            }
            lastFertOn = data.fertilizerStatus
        }

        // ค่า pH: บันทึกตอนเพิ่งออกนอกช่วง และตอนกลับเข้าช่วงปกติ
        val phOutOfRange = data.phValue < settings.phMin || data.phValue > settings.phMax
        if (phOutOfRange != lastPhOutOfRange) {
            if (phOutOfRange) {
                val detail = if (data.phValue > settings.phMax)
                    "ค่า pH สูงเกินช่วงที่ตั้งไว้ (%.1f เกิน %.1f)".format(data.phValue, settings.phMax)
                else
                    "ค่า pH ต่ำกว่าช่วงที่ตั้งไว้ (%.1f ต่ำกว่า %.1f)".format(data.phValue, settings.phMin)
                addLog(LogKind.WARNING, detail)
            } else {
                addLog(LogKind.INFO, "ค่า pH กลับเข้าสู่ช่วงปกติแล้ว (%.1f)".format(data.phValue))
            }
            lastPhOutOfRange = phOutOfRange
        }
    }

    @Synchronized
    private fun addLog(kind: LogKind, message: String) {
        val entry = LogEntry(kind, message, System.currentTimeMillis())
        val cutoff = System.currentTimeMillis() - LOG_RETENTION_MS
        val current = _logs.value ?: emptyList()
        val updated = (listOf(entry) + current)
            .filter { it.timestampMillis >= cutoff } // ลบ log ที่เก่าเกิน 7 วันทิ้งอัตโนมัติ
            .take(50)
        _logs.postValue(updated)
    }

    /** ล้าง log ทั้งหมดด้วยตนเอง (ปุ่ม "ล้าง log" ในหน้าประวัติ) */
    @Synchronized
    fun clearLogs() {
        _logs.postValue(emptyList())
    }

    /** แจ้งเตือนเฉพาะตอนที่สถานะ "เปลี่ยน" จากเชื่อมต่อ↔ไม่เชื่อมต่อ ไม่แจ้งซ้ำระหว่างที่ค้างอยู่สถานะเดิม */
    private fun checkConnectionNotification(state: ConnectionState) {
        val isConnected = state == ConnectionState.CONNECTED
        if (isConnected && !wasConnected) {
            notificationHelper.showConnected()
        } else if (!isConnected && wasConnected) {
            notificationHelper.showDisconnected()
        }
        wasConnected = isConnected
    }

    /** แจ้งเตือนตอนค่าเพิ่งออกนอกช่วงที่ตั้งไว้เท่านั้น (edge-triggered) แล้วเงียบไว้จนกว่าจะกลับเข้าช่วงปกติก่อนถึงจะแจ้งซ้ำได้อีก */
    private fun checkAlertNotifications(data: SensorData) {
        val phOutOfRange = data.phValue < settings.phMin || data.phValue > settings.phMax
        if (phOutOfRange && !phAlertActive) {
            notificationHelper.showPhAlert(data.phValue, settings.phMin, settings.phMax)
        }
        phAlertActive = phOutOfRange

        val moistureLow = data.soilMoisture < settings.moistureThreshold
        if (moistureLow && !moistureAlertActive) {
            notificationHelper.showMoistureAlert(data.soilMoisture, settings.moistureThreshold)
        }
        moistureAlertActive = moistureLow
    }

    override fun onCleared() {
        super.onCleared()
        httpRepository.stopPolling()
    }
}

private const val HISTORY_SAMPLE_INTERVAL_MS = 5 * 60 * 1000L  // เก็บจุดกราฟทุก 5 นาที (หลังพ้นช่วงวอร์มอัพ)
private const val HISTORY_MAX_POINTS = 96                        // 96 x 5 นาที = 8 ชั่วโมงย้อนหลัง
private const val WARMUP_SAMPLE_INTERVAL_MS = 15 * 1000L         // ช่วงแรกเก็บถี่ทุก 15 วินาที ให้เห็นกราฟไว
private const val WARMUP_POINT_COUNT = 8                          // พอครบ 8 จุด (~2 นาที) ค่อยสลับไปช้าลง
private const val LOG_RETENTION_MS = 7 * 24 * 60 * 60 * 1000L    // ลบ log ที่เก่าเกิน 7 วันทิ้งอัตโนมัติ

enum class CommandAction { WATER, FERTILIZER }

data class CommandFeedback(val action: CommandAction, val success: Boolean)
