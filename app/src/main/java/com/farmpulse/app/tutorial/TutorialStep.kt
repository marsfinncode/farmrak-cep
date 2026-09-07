package com.farmpulse.app.tutorial

/**
 * หนึ่งขั้นตอนของ tutorial — ถ้า targetViewId เป็น null แปลว่าไม่มีจุดเจาะจง (เช่น step แนะนำตัว)
 * tabId ระบุว่าต้องสลับไปแท็บล่างไหนก่อนถึงจะหา targetViewId เจอ (null = ไม่ต้องสลับแท็บ)
 */
data class TutorialStep(
    val tabId: Int?,
    val targetViewId: Int?,
    val title: String,
    val description: String
)
