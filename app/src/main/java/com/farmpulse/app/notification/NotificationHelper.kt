package com.farmpulse.app.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.farmpulse.app.R

/**
 * จัดการแจ้งเตือนของแอป: บอร์ดเชื่อมต่อแล้ว/ยกเลิกการเชื่อมต่อ, ค่า pH ผิดปกติ, ความชื้นดินต่ำ
 *
 * ใช้ทั้งตอนแอปเปิดอยู่ (เรียกจาก SensorViewModel) และตอนแอปอยู่เบื้องหลัง (เรียกจาก BoardMonitorService)
 */
class NotificationHelper(private val context: Context) {

    init {
        createChannel()
        createMonitorChannel()
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "แจ้งเตือนสถานะฟาร์ม",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "แจ้งเตือนการเชื่อมต่อบอร์ดและค่าเซนเซอร์ผิดปกติ"
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    /** แชแนลแยกต่างหาก สำหรับข้อความ "กำลังตรวจสอบ" ที่ต้องค้างแสดงตลอดตอนทำงานเบื้องหลัง
     *  ตั้ง IMPORTANCE_LOW ไว้ตั้งใจ — ไม่มีเสียง ไม่สั่น ไม่รบกวน ต่างจากแชแนลแจ้งเตือนจริงด้านบน */
    private fun createMonitorChannel() {
        val channel = NotificationChannel(
            MONITOR_CHANNEL_ID,
            "กำลังตรวจสอบเบื้องหลัง",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "ข้อความแสดงว่าแอปกำลังตรวจสอบบอร์ดอยู่เบื้องหลัง"
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun notify(id: Int, title: String, message: String, colorRes: Int) {
        if (!hasPermission()) return
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sprout)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setColor(ContextCompat.getColor(context, colorRes))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
    }

    fun showConnected() {
        notify(
            ID_CONNECTION,
            "เชื่อมต่อบอร์ดแล้ว",
            "แอปเชื่อมต่อกับบอร์ด ESP32 สำเร็จ",
            R.color.leaf
        )
    }

    fun showDisconnected() {
        notify(
            ID_CONNECTION,
            "บอร์ดยกเลิกการเชื่อมต่อ",
            "แอปขาดการเชื่อมต่อกับบอร์ด ลองเช็ค WiFi หรือเปิดบอร์ดใหม่",
            R.color.rust
        )
    }

    fun showPhAlert(value: Float, min: Float, max: Float) {
        notify(
            ID_PH,
            "ค่า pH ผิดปกติ",
            "ค่า pH ปัจจุบัน ${"%.1f".format(value)} อยู่นอกช่วงที่ตั้งไว้ ($min–$max)",
            R.color.amber_text
        )
    }

    fun showMoistureAlert(value: Float, threshold: Int) {
        notify(
            ID_MOISTURE,
            "ความชื้นในดินต่ำ",
            "ความชื้นปัจจุบัน ${value.toInt()}% ต่ำกว่าเกณฑ์ที่ตั้งไว้ ($threshold%)",
            R.color.water
        )
    }

    companion object {
        const val CHANNEL_ID = "farmrak_alerts"
        const val MONITOR_CHANNEL_ID = "farmrak_monitor"
        private const val ID_CONNECTION = 1001
        private const val ID_PH = 1002
        private const val ID_MOISTURE = 1003
    }
}
