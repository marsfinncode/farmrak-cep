package com.farmpulse.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.farmpulse.app.data.SettingsRepository
import com.farmpulse.app.data.UserProfileRepository
import com.farmpulse.app.databinding.ActivityMainBinding
import com.farmpulse.app.notification.BoardMonitorService
import com.farmpulse.app.notification.NotificationHelper
import com.farmpulse.app.tutorial.TutorialController
import com.farmpulse.app.ui.DashboardFragment
import com.farmpulse.app.ui.HistoryFragment
import com.farmpulse.app.ui.PlantsFragment
import com.farmpulse.app.ui.SettingsFragment
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.farmpulse.app.viewmodel.SensorViewModel
import com.farmpulse.app.viewmodel.SensorViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var backPressedTime: Long = 0
    private var backToast: Toast? = null

    // ViewModel เดียวใช้ร่วมกันทั้ง 4 แท็บ เพื่อให้ข้อมูลจากบอร์ด sync กันทุกหน้าจอ
    val sensorViewModel: SensorViewModel by viewModels {
        SensorViewModelFactory(SettingsRepository(applicationContext), NotificationHelper(applicationContext))
    }

    // ต้อง register ก่อน Activity เข้าสถานะ CREATED เลยประกาศเป็น property ตรงนี้
    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* ไม่ว่าจะอนุญาตหรือปฏิเสธ ก็ปล่อยให้แอปทำงานต่อได้ตามปกติ แค่จะไม่มีแจ้งเตือนถ้าไม่อนุญาต */ }

    private val serviceHandler = Handler(Looper.getMainLooper())
    private val startMonitorServiceRunnable = Runnable {
        try {
            ContextCompat.startForegroundService(this, Intent(this, BoardMonitorService::class.java))
        } catch (e: Exception) {
            // ถ้าระบบปฏิเสธการเริ่ม foreground service ด้วยเหตุผลใดก็ตาม ไม่ให้แอปเด้ง แค่ไม่มี background monitor รอบนั้นไป
        }
    }

    private val tutorialController: TutorialController by lazy {
        TutorialController(this, binding, UserProfileRepository(applicationContext))
    }

    /** เรียกจากหน้าตั้งค่า ("วิธีใช้แอป") เพื่อดูทัวร์แนะนำฟีเจอร์ซ้ำได้ตลอดเวลา */
    fun startTutorial() {
        binding.bottomNav.selectedItemId = R.id.nav_dashboard
        binding.root.post { tutorialController.start() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    backToast?.cancel()
                    finish()
                } else {
                    backToast = Toast.makeText(this@MainActivity, "กดอีกครั้งเพื่อออกจากแอป", Toast.LENGTH_SHORT)
                    backToast?.show()
                }
                backPressedTime = System.currentTimeMillis()
            }
        })

        requestNotificationPermissionIfNeeded()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, DashboardFragment())
                .commit()
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_dashboard -> DashboardFragment()
                R.id.nav_history -> HistoryFragment()
                R.id.nav_settings -> SettingsFragment()
                R.id.nav_plants -> PlantsFragment()
                R.id.nav_manual -> com.farmpulse.app.ui.ManualFragment()
                else -> DashboardFragment()
            }
            supportFragmentManager.beginTransaction()
                // จางเข้า-ออกตอนสลับแท็บ แทนที่จะกระตุกเปลี่ยนทันทีแบบเดิม
                .setCustomAnimations(R.anim.fragment_fade_in, R.anim.fragment_fade_out)
                .replace(R.id.fragmentContainer, fragment)
                .commit()
            true
        }

        // เริ่มถามค่าจากบอร์ดทันทีที่เปิดแอป (ต้องต่อ WiFi ของบอร์ดไว้ก่อนแล้วที่ระบบ)
        sensorViewModel.connectToBoard()

        if (savedInstanceState == null && intent.getBooleanExtra(EXTRA_START_TUTORIAL, false)) {
            // หน่วงเวลาเล็กน้อยให้ Dashboard วาดเสร็จก่อน ค่อยเริ่ม tutorial
            binding.root.postDelayed({ tutorialController.start() }, 400)
        }
    }

    /** Android 13 (API 33) ขึ้นไป ต้องขอสิทธิ์ก่อนถึงจะแจ้งเตือนได้ — เวอร์ชันก่อนหน้าไม่ต้องขอ */
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // ยกเลิกคำสั่งเริ่ม service ที่อาจตั้งเวลาไว้ (ถ้ายังไม่ทันเริ่มจริง) — สำคัญมากตอนสลับแอปเข้า-ออกเร็วๆ
        serviceHandler.removeCallbacks(startMonitorServiceRunnable)
        // แอปกลับมาอยู่หน้าจอ — ให้ ViewModel poll ถี่ตามปกติ ไม่ต้องให้ background service ทำงานซ้ำซ้อน
        try {
            stopService(Intent(this, BoardMonitorService::class.java))
        } catch (e: Exception) {
            // ไม่ให้แอปเด้งไม่ว่ากรณีใด แค่ปล่อยผ่านไป
        }
        sensorViewModel.resumePolling()
    }

    override fun onStop() {
        super.onStop()
        // แอปสลับไปเบื้องหลัง — หยุด poll ถี่ๆ ของ ViewModel แล้วให้ background service คอยเช็คแทนด้วยความถี่ต่ำกว่ามาก
        sensorViewModel.pausePolling()

        // หน่วงเวลาก่อนเริ่ม service จริง (ไม่เริ่มทันที) — ป้องกัน race condition ตอนผู้ใช้สลับแอปเข้า-ออกเร็วมาก
        // ที่ทำให้ stopService() (ใน onStart) ไปถึงระบบก่อนที่ service จะทันเรียก startForeground()
        // จนระบบมองว่า "เริ่ม foreground service แล้วไม่ยอม startForeground()" แล้วบังคับปิดแอปทั้งหมด
        // ถ้าผู้ใช้กลับมาเร็วกว่านี้ onStart() จะยกเลิกคำสั่งนี้ทันควันก่อนที่มันจะทำงานจริง
        serviceHandler.removeCallbacks(startMonitorServiceRunnable)
        serviceHandler.postDelayed(startMonitorServiceRunnable, SERVICE_START_DELAY_MS)
    }

    companion object {
        private const val SERVICE_START_DELAY_MS = 800L
        const val EXTRA_START_TUTORIAL = "extra_start_tutorial"
    }
}
