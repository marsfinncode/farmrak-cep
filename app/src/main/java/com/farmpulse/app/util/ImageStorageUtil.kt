package com.farmpulse.app.util

import android.content.Context
import android.net.Uri
import java.io.File

object ImageStorageUtil {
    /**
     * คัดลอกรูปที่ผู้ใช้เลือกจากแกลเลอรีมาเก็บไว้ในพื้นที่ของแอปเอง (filesDir)
     * เพื่อให้ยังเปิดดูได้แม้สิทธิ์เข้าถึง URI เดิมจากแกลเลอรีจะหมดอายุไปแล้ว
     */
    fun copyToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val input = context.contentResolver.openInputStream(uri) ?: return null
            val file = File(context.filesDir, "profile_${System.currentTimeMillis()}.jpg")
            input.use { inp -> file.outputStream().use { out -> inp.copyTo(out) } }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}
