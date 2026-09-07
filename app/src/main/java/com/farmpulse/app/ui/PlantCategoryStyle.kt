package com.farmpulse.app.ui

import com.farmpulse.app.R
import com.farmpulse.app.data.PlantCategory

object PlantCategoryStyle {
    fun iconRes(category: PlantCategory): Int = when (category) {
        PlantCategory.HERB -> R.drawable.ic_plant_herb
        PlantCategory.LEAFY -> R.drawable.ic_plant_leafy
        PlantCategory.FRUIT_VEG -> R.drawable.ic_plant_fruit
        PlantCategory.VINE -> R.drawable.ic_plant_vine
        PlantCategory.ROOT -> R.drawable.ic_plant_root
        PlantCategory.TREE -> R.drawable.ic_plant_tree
        PlantCategory.FLOWER -> R.drawable.ic_plant_flower
    }

    fun bgColorRes(category: PlantCategory): Int = when (category) {
        PlantCategory.HERB -> R.color.leaf_card
        PlantCategory.LEAFY -> R.color.leaf_card
        PlantCategory.FRUIT_VEG -> R.color.amber_card
        PlantCategory.VINE -> R.color.water_card
        PlantCategory.ROOT -> R.color.amber_card
        PlantCategory.TREE -> R.color.leaf_card
        PlantCategory.FLOWER -> R.color.blossom_card
    }

    fun categoryLabel(category: PlantCategory): String = when (category) {
        PlantCategory.HERB -> "สมุนไพร"
        PlantCategory.LEAFY -> "ผักใบ"
        PlantCategory.FRUIT_VEG -> "ผักผล"
        PlantCategory.VINE -> "ไม้เลื้อย"
        PlantCategory.ROOT -> "พืชหัว"
        PlantCategory.TREE -> "ไม้ยืนต้น"
        PlantCategory.FLOWER -> "ดอกไม้"
    }
}
