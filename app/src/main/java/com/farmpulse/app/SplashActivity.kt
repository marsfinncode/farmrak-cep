package com.farmpulse.app

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.farmpulse.app.data.UserProfileRepository
import com.farmpulse.app.databinding.ActivitySplashBinding

/**
 * หน้าอินโทรของแอป — เล่นแอนิเมชันครั้งเดียวตอนเปิดแอปแล้วไปหน้าหลักเอง
 * ใช้แค่ alpha/translationY (ผ่าน ViewPropertyAnimator) และ ValueAnimator ธรรมดา
 * ซึ่งเบามาก และ "เคารพ" การตั้งค่าลดการเคลื่อนไหวของระบบ (Settings > Accessibility)
 * โดยอัตโนมัติ เพราะ Animator ของ Android จะ scale ตามค่านั้นอยู่แล้ว
 */
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private var navigated = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val reduceMotion = isSystemReduceMotionEnabled()

        binding.splashRoot.setOnClickListener { navigateToMain() }

        if (reduceMotion) {
            // ข้ามแอนิเมชัน แสดงทุกอย่างทันที แล้วไปหน้าหลักเร็วขึ้น
            binding.splashProgress.progress = binding.splashProgress.max
            handler.postDelayed({ navigateToMain() }, 400)
            return
        }

        // ไอคอน/ชื่อ/คำอธิบาย/แท็กโครงการ ค่อยๆ ปรากฏทีละอย่าง (fade + slide เบาๆ)
        animateIn(binding.splashIconWrap, 100)
        animateIn(binding.splashTitle, 280)
        animateIn(binding.splashSub, 380)
        animateIn(binding.splashProgram, 460)

        // แถบโหลดค่อยๆ เติมครั้งเดียว
        handler.postDelayed({ animateProgress() }, 500)

        // ไปหน้าหลักอัตโนมัติหลังจากดูอินโทรครบ (ผู้ใช้แตะหน้าจอเพื่อข้ามได้ตลอดเวลา)
        handler.postDelayed({ navigateToMain() }, 1900)
    }

    private fun animateIn(view: android.view.View, startDelay: Long) {
        view.alpha = 0f
        view.translationY = 24f
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(startDelay)
            .setDuration(450)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    private fun animateProgress() {
        val animator = ValueAnimator.ofInt(0, binding.splashProgress.max)
        animator.duration = 1100
        animator.addUpdateListener { binding.splashProgress.progress = it.animatedValue as Int }
        animator.start()
    }

    private fun navigateToMain() {
        if (navigated) return
        navigated = true
        handler.removeCallbacksAndMessages(null)
        val profileRepo = UserProfileRepository(applicationContext)
        val nextActivity = if (profileRepo.onboardingCompleted) {
            MainActivity::class.java
        } else {
            OnboardingActivity::class.java
        }
        startActivity(Intent(this, nextActivity))
        finish()
    }

    /** เทียบเท่ากับ prefers-reduced-motion บนเว็บ: ผู้ใช้ปิดแอนิเมชันไว้ในตั้งค่าการช่วยเหลือพิเศษ */
    private fun isSystemReduceMotionEnabled(): Boolean {
        val scale = Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        return scale == 0f
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
