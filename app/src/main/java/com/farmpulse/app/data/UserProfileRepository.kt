package com.farmpulse.app.data

import android.content.Context
import android.content.SharedPreferences

/**
 * เก็บข้อมูลผู้ใช้ที่กรอกตอน onboarding (ชื่อที่อยากให้เรียก, อาชีพ, ปีประสบการณ์, รูปโปรไฟล์)
 * แก้ไขภายหลังได้ในหน้าตั้งค่า — ใช้ SharedPreferences เก็บในเครื่อง ไม่ส่งขึ้นเซิร์ฟเวอร์ใดๆ
 */
class UserProfileRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("farmpulse_profile", Context.MODE_PRIVATE)

    var onboardingCompleted: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_DONE, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_DONE, value).apply()

    var tutorialCompleted: Boolean
        get() = prefs.getBoolean(KEY_TUTORIAL_DONE, false)
        set(value) = prefs.edit().putBoolean(KEY_TUTORIAL_DONE, value).apply()

    var userName: String
        get() = prefs.getString(KEY_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_NAME, value).apply()

    var occupation: String
        get() = prefs.getString(KEY_OCCUPATION, "") ?: ""
        set(value) = prefs.edit().putString(KEY_OCCUPATION, value).apply()

    /** ค่าที่เลือกจาก dropdown ประสบการณ์ (เช่น "มีประสบการณ์ปานกลาง (1–5 ปี)") */
    var farmingExperience: String
        get() = prefs.getString(KEY_EXPERIENCE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_EXPERIENCE, value).apply()

    /** path ไฟล์รูปที่ copy มาเก็บไว้ในพื้นที่ของแอปเอง (ไม่ใช่ URI ชั่วคราวจากแกลเลอรี) */
    var profileImagePath: String?
        get() = prefs.getString(KEY_IMAGE_PATH, null)
        set(value) {
            if (value == null) prefs.edit().remove(KEY_IMAGE_PATH).apply()
            else prefs.edit().putString(KEY_IMAGE_PATH, value).apply()
        }

    /** ชื่อที่ใช้แสดงในแอป — ถ้ายังไม่ได้กรอกชื่อ ใช้คำเรียกกลางๆ แทน */
    fun displayName(): String = userName.ifBlank { "เกษตรกร" }

    companion object {
        private const val KEY_ONBOARDING_DONE = "onboarding_completed"
        private const val KEY_TUTORIAL_DONE = "tutorial_completed"
        private const val KEY_NAME = "user_name"
        private const val KEY_OCCUPATION = "user_occupation"
        private const val KEY_EXPERIENCE = "farming_experience"
        private const val KEY_IMAGE_PATH = "profile_image_path"
    }
}
