package com.farmpulse.app.tutorial

import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import com.farmpulse.app.MainActivity
import com.farmpulse.app.R
import com.farmpulse.app.data.UserProfileRepository
import com.farmpulse.app.util.AnimUtils
import com.farmpulse.app.databinding.ActivityMainBinding

class TutorialController(
    private val activity: MainActivity,
    private val binding: ActivityMainBinding,
    private val profileRepo: UserProfileRepository
) {
    private var currentIndex = 0
    private val marginPx = 14f * activity.resources.displayMetrics.density

    private val steps: List<TutorialStep> by lazy {
        listOf(
            TutorialStep(R.id.nav_dashboard, null,
                activity.getString(R.string.tutorial_step1_title), activity.getString(R.string.tutorial_step1_desc)),
            TutorialStep(R.id.nav_dashboard, R.id.tvGreeting,
                activity.getString(R.string.tutorial_step2_title), activity.getString(R.string.tutorial_step2_desc)),
            TutorialStep(R.id.nav_dashboard, R.id.tvMoistureValue,
                activity.getString(R.string.tutorial_step3_title), activity.getString(R.string.tutorial_step3_desc)),
            TutorialStep(R.id.nav_dashboard, R.id.tvPhValue,
                activity.getString(R.string.tutorial_step_ph_title), activity.getString(R.string.tutorial_step_ph_desc)),
            TutorialStep(R.id.nav_dashboard, R.id.switchAutoMode,
                activity.getString(R.string.tutorial_step4_title), activity.getString(R.string.tutorial_step4_desc)),
            TutorialStep(R.id.nav_dashboard, R.id.btnWater,
                activity.getString(R.string.tutorial_step5_title), activity.getString(R.string.tutorial_step5_desc)),
            TutorialStep(R.id.nav_dashboard, R.id.btnSyncTime,
                activity.getString(R.string.tutorial_step_synctime_title), activity.getString(R.string.tutorial_step_synctime_desc)),
            TutorialStep(R.id.nav_history, R.id.sparklineMoisture,
                activity.getString(R.string.tutorial_step6_title), activity.getString(R.string.tutorial_step6_desc)),
            TutorialStep(R.id.nav_plants, R.id.etSearchPlant,
                activity.getString(R.string.tutorial_step7_title), activity.getString(R.string.tutorial_step7_desc)),
            TutorialStep(R.id.nav_settings, R.id.seekMoistureThreshold,
                activity.getString(R.string.tutorial_step_autowater_title), activity.getString(R.string.tutorial_step_autowater_desc)),
            TutorialStep(R.id.nav_settings, R.id.btnAddSchedule,
                activity.getString(R.string.tutorial_step_fertschedule_title), activity.getString(R.string.tutorial_step_fertschedule_desc)),
            TutorialStep(R.id.nav_settings, R.id.etBoardIp,
                activity.getString(R.string.tutorial_step_board_final_title), activity.getString(R.string.tutorial_step_board_final_desc))
        )
    }

    fun start() {
        currentIndex = 0
        binding.spotlightOverlay.visibility = View.VISIBLE
        binding.tutorialTooltip.visibility = View.VISIBLE
        binding.tutorialTooltip.alpha = 0f
        binding.tutorialTooltip.animate().alpha(1f).setDuration(220)
            .setInterpolator(AnimUtils.emphasized()).start()

        binding.btnTutorialNext.setOnClickListener { next() }
        binding.btnTutorialPrev.setOnClickListener { previous() }
        binding.btnTutorialSkip.setOnClickListener { finish() }
        binding.spotlightOverlay.setOnClickListener { next() }

        showStep(0)
    }

    private fun next() {
        if (currentIndex < steps.size - 1) {
            currentIndex++
            showStep(currentIndex)
        } else {
            finish()
        }
    }

    private fun previous() {
        if (currentIndex > 0) {
            currentIndex--
            showStep(currentIndex)
        }
    }

    private fun showStep(index: Int) {
        val step = steps[index]

        binding.tvTutorialCounter.text =
            activity.getString(R.string.tutorial_step_counter, index + 1, steps.size)
        binding.tvTutorialTitle.text = step.title
        binding.tvTutorialDescription.text = step.description
        binding.btnTutorialNext.text =
            if (index == steps.size - 1) activity.getString(R.string.tutorial_done)
            else activity.getString(R.string.tutorial_next)

        // ปุ่มย้อนกลับ กดไม่ได้ตอนอยู่ขั้นแรกสุด (ไม่มีอะไรให้ย้อนไป)
        binding.btnTutorialPrev.isEnabled = index > 0
        binding.btnTutorialPrev.alpha = if (index > 0) 1f else 0.4f

        // สลับแท็บก่อนถ้า step นี้ต้องการหน้าจออื่น แล้วรอให้ fragment ใหม่ถูกสร้างเสร็จจริงก่อนหาตำแหน่ง view
        if (step.tabId != null && binding.bottomNav.selectedItemId != step.tabId) {
            binding.bottomNav.selectedItemId = step.tabId
            activity.supportFragmentManager.executePendingTransactions()
        }

        if (step.targetViewId == null) {
            showCenteredFallback()
            return
        }

        // post สองชั้น: รอ layout pass ของ fragment ใหม่ให้เสร็จสมบูรณ์ก่อนค่อยหาตำแหน่ง view
        binding.root.post {
            binding.root.post {
                val target = binding.root.findViewById<View>(step.targetViewId)
                if (target == null || target.visibility != View.VISIBLE || target.width == 0 || target.height == 0) {
                    // หา view ไม่เจอ/ซ่อนอยู่ (เช่น ยังไม่เชื่อมต่อบอร์ด ปุ่มบางปุ่มเลยยังไม่โผล่)
                    // แทนที่จะปล่อยว่างเปล่าดูเหมือนค้าง ให้ fallback เป็นกล่องข้อความกลางจอเหมือน step ที่ไม่มีจุดเจาะจง
                    showCenteredFallback()
                    return@post
                }

                // เลื่อนจอให้ target ขึ้นมาอยู่ "ช่วงบน-กลาง" ของหน้าจอเสมอ (ไม่ปล่อยให้อยู่ล่างสุด)
                // เพราะถ้าสปอตไลท์อยู่ล่าง กล่องข้อความจะไม่มีที่วางและอ่านยาก
                scrollTargetIntoUpperArea(target)

                // รอให้ smooth scroll เลื่อนจนสุดก่อน ค่อยวัดตำแหน่งสุดท้ายจริงๆ
                binding.root.postDelayed({
                    val rect = Rect()
                    target.getGlobalVisibleRect(rect)
                    val rootRect = Rect()
                    binding.root.getGlobalVisibleRect(rootRect)
                    val rectF = RectF(
                        (rect.left - rootRect.left).toFloat(),
                        (rect.top - rootRect.top).toFloat(),
                        (rect.right - rootRect.left).toFloat(),
                        (rect.bottom - rootRect.top).toFloat()
                    )
                    if (rectF.width() <= 0f || rectF.height() <= 0f) {
                        // เลื่อนจอแล้วยังวัดขนาดไม่ได้ (กรณีสุดวิสัย) — fallback กันไว้อีกชั้น
                        showCenteredFallback()
                        return@postDelayed
                    }
                    binding.spotlightOverlay.animateTo(rectF)
                    positionTooltip(rectF)
                }, 320)
            }
        }
    }

    /**
     * เลื่อน ScrollView ที่ครอบ target อยู่ ให้ target ไปโผล่บริเวณ "หนึ่งในสามบนของจอ"
     * แทนที่จะใช้ requestRectangleOnScreen ซึ่งเลื่อนแค่พอให้เห็น (target มักไปจอดอยู่ขอบล่างสุด)
     * ทำให้มีที่ว่างด้านล่างพอสำหรับวางกล่องข้อความเสมอ และผู้ใช้เห็นจุดที่ชี้ได้ชัดกว่า
     */
    private fun scrollTargetIntoUpperArea(target: View) {
        val scrollParent = findScrollParent(target) ?: return

        val targetLoc = IntArray(2)
        target.getLocationOnScreen(targetLoc)
        val scrollLoc = IntArray(2)
        scrollParent.getLocationOnScreen(scrollLoc)

        // ตำแหน่ง target เทียบกับขอบบนของ ScrollView ในปัจจุบัน
        val targetTopInScroll = targetLoc[1] - scrollLoc[1] + scrollParent.scrollY
        // อยากให้ target ไปอยู่ที่ประมาณ 1/3 จากขอบบนของพื้นที่ที่มองเห็น
        val desiredOffset = (scrollParent.height * 0.28f).toInt()
        val newScrollY = (targetTopInScroll - desiredOffset).coerceAtLeast(0)

        scrollParent.smoothScrollTo(0, newScrollY)
    }

    /** ไล่หา ScrollView ที่ครอบ view นี้อยู่ (ถ้ามี) */
    private fun findScrollParent(view: View): android.widget.ScrollView? {
        var p = view.parent
        while (p != null) {
            if (p is android.widget.ScrollView) return p
            p = (p as? View)?.parent
        }
        return null
    }

    /** ใช้ตอนไม่มีจุดเจาะจง หรือหา/วัดตำแหน่ง target ไม่ได้ — วางกล่องข้อความไว้กลางจอ ไม่มีสปอตไลท์ แทนที่จะดูค้างว่างเปล่า */
    private fun showCenteredFallback() {
        binding.spotlightOverlay.clearSpotlight()
        binding.tutorialTooltip.post {
            val centerY = (binding.root.height - binding.tutorialTooltip.height) / 2f
            binding.tutorialTooltip.animate().y(centerY).setDuration(260)
                .setInterpolator(AnimUtils.emphasized()).start()
        }
    }

    /** วางกล่องข้อความไว้ในฝั่งที่มีที่ว่างมากกว่าเสมอ (บนหรือล่างของจุดสปอตไลท์) กันไม่ให้ไปทับปุ่ม/องค์ประกอบที่กำลังชี้อยู่ */
    private fun positionTooltip(spotlightRect: RectF) {
        val tooltip = binding.tutorialTooltip
        tooltip.post {
            val tooltipHeight = tooltip.height.toFloat()
            val bottomLimit = binding.bottomNav.y // ขอบบนของแถบเมนูล่าง ไม่ให้กล่องข้อความล้ำลงไปทับ
            val spaceBelow = bottomLimit - spotlightRect.bottom
            val spaceAbove = spotlightRect.top

            val targetY = if (spaceBelow >= tooltipHeight + marginPx * 2 || spaceBelow >= spaceAbove) {
                // วางไว้ใต้จุดสปอตไลท์
                (spotlightRect.bottom + marginPx).coerceAtMost(bottomLimit - tooltipHeight - marginPx)
            } else {
                // ที่ว่างด้านล่างไม่พอ (จุดสปอตไลท์อยู่ต่ำเกินไป) — วางไว้เหนือจุดสปอตไลท์แทน
                (spotlightRect.top - tooltipHeight - marginPx).coerceAtLeast(marginPx)
            }
            tooltip.animate().y(targetY).setDuration(260)
                .setInterpolator(AnimUtils.emphasized()).start()
        }
    }

    private fun finish() {
        profileRepo.tutorialCompleted = true
        binding.tutorialTooltip.animate().alpha(0f).setDuration(150).withEndAction {
            binding.tutorialTooltip.visibility = View.GONE
            binding.spotlightOverlay.visibility = View.GONE
            binding.spotlightOverlay.clearSpotlight()
        }.start()
    }
}
