package com.farmpulse.app.util

/**
 * ใช้ห่อค่าที่ควรเกิดขึ้น "ครั้งเดียว" เช่น Toast/Snackbar แจ้งผลการส่งคำสั่ง
 * ป้องกันไม่ให้ LiveData ส่งค่าเดิมซ้ำอีกครั้งตอน Fragment ถูกสร้างใหม่ (เช่น สลับแท็บแล้วกลับมา)
 */
class Event<out T>(private val content: T) {

    private var hasBeenHandled = false

    /** คืนค่าเฉพาะครั้งแรกที่เรียก ครั้งถัดไปจะได้ null */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }
}
