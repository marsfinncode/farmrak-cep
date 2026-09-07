package com.farmpulse.app.data

enum class PlantCategory { HERB, LEAFY, FRUIT_VEG, VINE, ROOT, TREE, FLOWER }

data class PlantReference(val label: String, val url: String)

data class Plant(
    val id: String,
    val thaiName: String,
    val scientificName: String,
    val category: PlantCategory,
    val photoRes: Int? = null,
    val photoPath: String? = null,
    val isCustom: Boolean = false,
    val description: String,
    val phMin: Float,
    val phMax: Float,
    val moistureMin: Int,
    val moistureMax: Int,
    val fertilizerAdvice: String,
    val references: List<PlantReference> = emptyList()
)
