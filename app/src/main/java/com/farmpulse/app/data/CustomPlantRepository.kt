package com.farmpulse.app.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * เก็บพืชที่ผู้ใช้เพิ่มเอง (นอกเหนือจาก 15 พืชสำเร็จรูป) ไว้ในเครื่อง เป็น JSON array ใน SharedPreferences
 * ไม่มีบรรณานุกรมสำหรับพืชกลุ่มนี้ เพราะเป็นข้อมูลที่ผู้ใช้บันทึกจากประสบการณ์ตัวเอง ไม่ใช่ข้อมูลอ้างอิงวิชาการ
 */
class CustomPlantRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("farmpulse_custom_plants", Context.MODE_PRIVATE)

    fun getAll(): List<Plant> {
        val json = prefs.getString(KEY_PLANTS, null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { i -> fromJson(array.getJSONObject(i)) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** คืนค่า true ถ้าบันทึกสำเร็จจริง — ใช้ commit() แทน apply() เพื่อให้รู้ผลจริงทันที ไม่ใช่แค่สมมติว่าสำเร็จ */
    fun add(plant: Plant): Boolean {
        val current = getAll().toMutableList()
        current.add(plant)
        return saveAll(current)
    }

    fun update(plant: Plant): Boolean {
        val current = getAll().toMutableList()
        val index = current.indexOfFirst { it.id == plant.id }
        if (index >= 0) {
            current[index] = plant
        } else {
            current.add(plant)
        }
        return saveAll(current)
    }

    fun delete(id: String): Boolean {
        val current = getAll().toMutableList()
        current.removeAll { it.id == id }
        return saveAll(current)
    }

    fun newId(): String = "custom_${System.currentTimeMillis()}"

    private fun saveAll(plants: List<Plant>): Boolean {
        val array = JSONArray()
        plants.forEach { array.put(toJson(it)) }
        return try {
            prefs.edit().putString(KEY_PLANTS, array.toString()).commit()
        } catch (e: Exception) {
            false
        }
    }

    private fun toJson(plant: Plant): JSONObject {
        val o = JSONObject()
        o.put("id", plant.id)
        o.put("thaiName", plant.thaiName)
        o.put("scientificName", plant.scientificName)
        o.put("category", plant.category.name)
        o.put("photoPath", plant.photoPath ?: JSONObject.NULL)
        o.put("description", plant.description)
        o.put("phMin", plant.phMin.toDouble())
        o.put("phMax", plant.phMax.toDouble())
        o.put("moistureMin", plant.moistureMin)
        o.put("moistureMax", plant.moistureMax)
        o.put("fertilizerAdvice", plant.fertilizerAdvice)
        return o
    }

    private fun fromJson(o: JSONObject): Plant {
        return Plant(
            id = o.getString("id"),
            thaiName = o.getString("thaiName"),
            scientificName = o.optString("scientificName", ""),
            category = try {
                PlantCategory.valueOf(o.getString("category"))
            } catch (e: Exception) {
                PlantCategory.HERB
            },
            photoPath = if (o.isNull("photoPath")) null else o.optString("photoPath", null),
            isCustom = true,
            description = o.optString("description", ""),
            phMin = o.optDouble("phMin", 5.5).toFloat(),
            phMax = o.optDouble("phMax", 7.5).toFloat(),
            moistureMin = o.optInt("moistureMin", 40),
            moistureMax = o.optInt("moistureMax", 70),
            fertilizerAdvice = o.optString("fertilizerAdvice", ""),
            references = emptyList()
        )
    }

    companion object {
        private const val KEY_PLANTS = "custom_plants_json"
    }
}
