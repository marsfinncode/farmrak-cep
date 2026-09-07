package com.farmpulse.app.notification

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.farmpulse.app.R
import com.farmpulse.app.data.SensorData
import com.farmpulse.app.data.SettingsRepository
import java.net.HttpURLConnection
import java.net.URL

/**
 * ทำงานเฉพาะตอนแอปอยู่เบื้องหลังเท่านั้น (MainActivity สั่งเริ่ม/หยุดตาม onStop/onStart)
 * ตรวจสอบบอร์ดด้วยความถี่ต่ำกว่าตอนเปิดแอปอยู่มาก (ทุก 20 วินาที แทนที่จะเป็น 4 วินาที)
 * เพื่อประหยัดแบตเตอรี่/ดาต้าให้เหมาะกับมือถือที่เกษตรกรใช้งานทั่วไป — ไม่ทำอะไรหนักๆ นอกจาก
 * เปิด connection สั้นๆ แล้วปิดทันที ไม่มีการประมวลผลหรือ UI ใดๆ ที่กินทรัพยากรเพิ่ม
 */
class BoardMonitorService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var settings: SettingsRepository

    private var wasConnected = false
    private var phAlertActive = false
    private var moistureAlertActive = false

    private val pollRunnable = object : Runnable {
        override fun run() {
            pollBoardStatus()
            handler.postDelayed(this, POLL_INTERVAL_MS)
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(applicationContext)
        settings = SettingsRepository(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            startForeground(SERVICE_NOTIF_ID, buildForegroundNotification())
        } catch (e: Exception) {
            // ถ้าเริ่ม foreground ไม่สำเร็จไม่ว่าเหตุผลใด ให้ปิดตัวเองเงียบๆ ดีกว่าปล่อยให้ทั้งแอปเด้ง
            stopSelf()
            return START_NOT_STICKY
        }
        handler.removeCallbacks(pollRunnable)
        handler.post(pollRunnable)
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun pollBoardStatus() {
        // รันบน thread แยกสั้นๆ ต่อครั้งเท่านั้น ไม่มี thread ค้างทำงานตลอดเวลา
        Thread {
            // ใช้รายชื่อ IP ชุดเดียวกับฝั่งแอป (ระบบค้นหาบอร์ดอัตโนมัติ)
            // ลองตัวที่เคยใช้ได้ก่อน ถ้าไม่ติดค่อยไล่ตัวอื่น — ครอบคลุมทั้งตอนอยู่บ้านและตอนอยู่ที่แปลง
            for (ip in settings.boardIpCandidates()) {
                val body = tryFetch(ip)
                if (body != null) {
                    try {
                        val data = SensorData.fromJson(body)
                        settings.lastWorkingIp = ip
                        handler.post { handleConnected(data) }
                    } catch (e: Exception) {
                        handler.post { handleDisconnected() }
                    }
                    return@Thread
                }
            }
            handler.post { handleDisconnected() }
        }.start()
    }

    private fun tryFetch(ip: String): String? {
        return try {
            val conn = URL("http://$ip/api/status").openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
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

    private fun handleConnected(data: SensorData) {
        if (!wasConnected) notificationHelper.showConnected()
        wasConnected = true
        checkAlerts(data)
    }

    private fun handleDisconnected() {
        if (wasConnected) notificationHelper.showDisconnected()
        wasConnected = false
    }

    private fun checkAlerts(data: SensorData) {
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

    private fun buildForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, NotificationHelper.MONITOR_CHANNEL_ID)
            .setContentTitle("FarmRak กำลังตรวจสอบบอร์ดอยู่เบื้องหลัง")
            .setContentText("จะแจ้งเตือนทันทีถ้าพบความผิดปกติ")
            .setSmallIcon(R.drawable.ic_sprout)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val SERVICE_NOTIF_ID = 2001
        private const val POLL_INTERVAL_MS = 20000L
    }
}
