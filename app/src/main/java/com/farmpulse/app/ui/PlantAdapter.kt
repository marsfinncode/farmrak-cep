package com.farmpulse.app.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.farmpulse.app.data.Plant
import com.farmpulse.app.databinding.ItemPlantBinding
import java.io.File

class PlantAdapter(
    private val onClick: (Plant) -> Unit
) : RecyclerView.Adapter<PlantAdapter.PlantViewHolder>() {

    private var items: List<Plant> = emptyList()

    fun submitList(newItems: List<Plant>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        val binding = ItemPlantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlantViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    class PlantViewHolder(
        private val binding: ItemPlantBinding,
        private val onClick: (Plant) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(plant: Plant) {
            binding.tvPlantName.text = plant.thaiName
            binding.tvPlantScientific.text = plant.scientificName
            binding.tvPlantCategory.text = PlantCategoryStyle.categoryLabel(plant.category)
            when {
                plant.photoPath != null && File(plant.photoPath).exists() -> {
                    binding.iconWrap.backgroundTintList = null
                    binding.imgPlantIcon.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                    binding.imgPlantIcon.setImageURI(Uri.fromFile(File(plant.photoPath)))
                }
                plant.photoRes != null -> {
                    binding.iconWrap.backgroundTintList = null
                    binding.imgPlantIcon.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                    binding.imgPlantIcon.setImageResource(plant.photoRes)
                }
                else -> {
                    // ยังไม่มีรูปถ่ายจริงสำหรับพืชนี้ — ใช้ไอคอนตามหมวดแทนไปก่อน
                    binding.iconWrap.backgroundTintList = ContextCompat.getColorStateList(
                        binding.root.context, PlantCategoryStyle.bgColorRes(plant.category)
                    )
                    binding.imgPlantIcon.scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
                    binding.imgPlantIcon.setImageResource(PlantCategoryStyle.iconRes(plant.category))
                }
            }
            binding.root.setOnClickListener { onClick(plant) }
        }
    }
}
