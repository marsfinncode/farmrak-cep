package com.farmpulse.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.farmpulse.app.R
import com.farmpulse.app.data.CustomPlantRepository
import com.farmpulse.app.data.Plant
import com.farmpulse.app.data.PlantCategory
import com.farmpulse.app.data.PlantData
import com.farmpulse.app.databinding.FragmentPlantsBinding
import com.farmpulse.app.util.AnimUtils
import com.farmpulse.app.util.ImageStorageUtil
import java.io.File

/** สถานะหน้าจอภายในแท็บตำราพืช — สลับกันด้วยการซ่อน/แสดง ไม่ใช้ fragment transaction เพื่อความง่าย */
private enum class PlantsScreen { LIST, DETAIL, FORM }

class PlantsFragment : Fragment() {

    private var _binding: FragmentPlantsBinding? = null
    private val binding get() = _binding!!
    private val adapter = PlantAdapter { plant -> showDetail(plant) }

    private lateinit var customPlantRepo: CustomPlantRepository
    private var currentDetailPlant: Plant? = null
    private var editingPlantId: String? = null
    private var pickedPhotoPath: String? = null
    private var currentScreen = PlantsScreen.LIST

    private val backCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            when (currentScreen) {
                PlantsScreen.FORM -> if (editingPlantId != null) showDetail(currentDetailPlant!!) else showList()
                PlantsScreen.DETAIL -> showList()
                PlantsScreen.LIST -> { /* ไม่ควรถูกเรียกตอนอยู่หน้า list เพราะปิด callback ไว้แล้ว */ }
            }
        }
    }

    private val pickFormImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            val path = ImageStorageUtil.copyToInternalStorage(requireContext(), uri)
            if (path != null) {
                pickedPhotoPath = path
                binding.imgFormPhoto.setImageURI(Uri.fromFile(File(path)))
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlantsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customPlantRepo = CustomPlantRepository(requireContext())

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        binding.recyclerPlants.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPlants.adapter = adapter
        refreshList()

        binding.etSearchPlant.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterPlants(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnBack.setOnClickListener { showList() }
        binding.fabAddPlant.setOnClickListener { showForm(null) }
        binding.btnCancelForm.setOnClickListener {
            if (editingPlantId != null) showDetail(currentDetailPlant!!) else showList()
        }
        binding.imgFormPhoto.setOnClickListener { openFormPhotoPicker() }

        binding.actFormCategory.setAdapter(
            ArrayAdapter(
                requireContext(), R.layout.item_dropdown,
                PlantCategory.values().map { PlantCategoryStyle.categoryLabel(it) }
            )
        )

        binding.btnEditPlant.setOnClickListener { currentDetailPlant?.let { showForm(it) } }
        binding.btnDeletePlant.setOnClickListener { currentDetailPlant?.let { confirmDelete(it) } }
        binding.btnDeletePlantForm.setOnClickListener {
            currentDetailPlant?.let { confirmDelete(it) }
        }
        binding.btnSavePlant.setOnClickListener { saveForm() }
    }

    private fun allPlants(): List<Plant> = PlantData.all + customPlantRepo.getAll()

    private fun refreshList(query: String = "") {
        filterPlants(query)
    }

    private fun filterPlants(query: String) {
        val source = allPlants()
        val filtered = if (query.isBlank()) {
            source
        } else {
            source.filter {
                it.thaiName.contains(query, ignoreCase = true) ||
                    it.scientificName.contains(query, ignoreCase = true)
            }
        }
        adapter.submitList(filtered)
        binding.tvNoResults.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerPlants.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun openFormPhotoPicker() {
        pickFormImage.launch(
            PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                .build()
        )
    }

    // ==================== Detail screen ====================

    private fun showDetail(plant: Plant) {
        currentDetailPlant = plant

        when {
            plant.photoPath != null && File(plant.photoPath).exists() -> {
                binding.imgDetailPhoto.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                binding.imgDetailPhoto.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                binding.imgDetailPhoto.setImageURI(Uri.fromFile(File(plant.photoPath)))
            }
            plant.photoRes != null -> {
                binding.imgDetailPhoto.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                binding.imgDetailPhoto.setBackgroundColor(android.graphics.Color.TRANSPARENT)
                binding.imgDetailPhoto.setImageResource(plant.photoRes)
            }
            else -> {
                binding.imgDetailPhoto.scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
                binding.imgDetailPhoto.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), PlantCategoryStyle.bgColorRes(plant.category))
                )
                binding.imgDetailPhoto.setImageResource(PlantCategoryStyle.iconRes(plant.category))
            }
        }

        binding.tvDetailName.text = plant.thaiName
        binding.tvDetailScientific.text = plant.scientificName
        binding.tvDetailScientific.visibility = if (plant.scientificName.isBlank()) View.GONE else View.VISIBLE
        binding.tvDetailDescription.text = plant.description
        binding.tvDetailPh.text = "${plant.phMin} – ${plant.phMax}"
        binding.tvDetailMoisture.text = "${plant.moistureMin}% – ${plant.moistureMax}%"
        binding.tvDetailFertilizer.text = plant.fertilizerAdvice

        val hasReferences = plant.references.isNotEmpty()
        binding.referencesDivider.visibility = if (hasReferences) View.VISIBLE else View.GONE
        binding.tvReferencesTitle.visibility = if (hasReferences) View.VISIBLE else View.GONE
        binding.referencesContainer.visibility = if (hasReferences) View.VISIBLE else View.GONE
        binding.referencesContainer.removeAllViews()
        plant.references.forEach { ref ->
            val tv = android.widget.TextView(requireContext()).apply {
                text = "•  ${ref.label}"
                setTextColor(ContextCompat.getColor(requireContext(), R.color.water))
                textSize = 13f
                setPadding(0, 6, 0, 6)
                setOnClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(ref.url)))
                }
            }
            binding.referencesContainer.addView(tv)
        }

        // ปุ่มแก้ไข/ลบ แสดงเฉพาะพืชที่ผู้ใช้เพิ่มเอง — พืชสำเร็จรูปแก้ไม่ได้
        binding.btnEditPlant.visibility = if (plant.isCustom) View.VISIBLE else View.GONE
        binding.btnDeletePlant.visibility = if (plant.isCustom) View.VISIBLE else View.GONE

        currentScreen = PlantsScreen.DETAIL
        binding.plantListContainer.visibility = View.GONE
        binding.plantFormContainer.visibility = View.GONE
        binding.plantDetailContainer.visibility = View.VISIBLE
        fadeIn(binding.plantDetailContainer)
        binding.fabAddPlant.visibility = View.GONE
        backCallback.isEnabled = true
    }

    // ==================== Form (add/edit) ====================

    private fun showForm(plant: Plant?) {
        editingPlantId = plant?.id
        pickedPhotoPath = null

        binding.tvFormTitle.text = if (plant == null) getString(R.string.add_plant_title) else getString(R.string.edit_plant_title)
        binding.etFormName.setText(plant?.thaiName ?: "")
        binding.etFormScientific.setText(plant?.scientificName ?: "")
        binding.etFormDescription.setText(plant?.description ?: "")
        binding.etFormFertilizer.setText(plant?.fertilizerAdvice ?: "")
        binding.etFormPhMin.setText(plant?.phMin?.toString() ?: "")
        binding.etFormPhMax.setText(plant?.phMax?.toString() ?: "")
        binding.etFormMoistureMin.setText(plant?.moistureMin?.toString() ?: "")
        binding.etFormMoistureMax.setText(plant?.moistureMax?.toString() ?: "")
        binding.actFormCategory.setText(
            PlantCategoryStyle.categoryLabel(plant?.category ?: PlantCategory.HERB), false
        )

        if (plant?.photoPath != null && File(plant.photoPath).exists()) {
            binding.imgFormPhoto.setImageURI(Uri.fromFile(File(plant.photoPath)))
        } else if (plant?.photoRes != null) {
            binding.imgFormPhoto.setImageResource(plant.photoRes)
        } else {
            binding.imgFormPhoto.setImageResource(R.drawable.ic_person_placeholder)
        }

        binding.btnDeletePlantForm.visibility = if (plant != null && plant.isCustom) View.VISIBLE else View.GONE

        currentScreen = PlantsScreen.FORM
        binding.plantListContainer.visibility = View.GONE
        binding.plantDetailContainer.visibility = View.GONE
        binding.plantFormContainer.visibility = View.VISIBLE
        fadeIn(binding.plantFormContainer)
        binding.fabAddPlant.visibility = View.GONE
        backCallback.isEnabled = true
    }

    private fun saveForm() {
        // ล้าง error เก่าทุกช่องก่อนตรวจใหม่ทุกครั้ง
        binding.etFormName.error = null
        binding.etFormPhMin.error = null
        binding.etFormPhMax.error = null
        binding.etFormMoistureMin.error = null
        binding.etFormMoistureMax.error = null

        val name = binding.etFormName.text.toString().trim()
        if (name.isEmpty()) {
            binding.etFormName.error = getString(R.string.plant_name_required_error)
            binding.etFormName.requestFocus()
            return
        }

        // ตรวจค่า pH จริงจัง — ไม่ปล่อยให้เงียบๆ ใช้ค่า default ทับถ้าผู้ใช้พิมพ์ผิด ต้องแจ้งให้แก้ก่อน
        val phMin = binding.etFormPhMin.text.toString().toFloatOrNull()
        val phMax = binding.etFormPhMax.text.toString().toFloatOrNull()
        if (phMin == null) {
            binding.etFormPhMin.error = getString(R.string.plant_ph_invalid_error)
            binding.etFormPhMin.requestFocus()
            return
        }
        if (phMax == null) {
            binding.etFormPhMax.error = getString(R.string.plant_ph_invalid_error)
            binding.etFormPhMax.requestFocus()
            return
        }
        if (phMin > phMax) {
            binding.etFormPhMin.error = getString(R.string.plant_ph_range_invalid_error)
            binding.etFormPhMin.requestFocus()
            return
        }

        val moistureMin = binding.etFormMoistureMin.text.toString().toIntOrNull()
        val moistureMax = binding.etFormMoistureMax.text.toString().toIntOrNull()
        if (moistureMin == null) {
            binding.etFormMoistureMin.error = getString(R.string.plant_moisture_invalid_error)
            binding.etFormMoistureMin.requestFocus()
            return
        }
        if (moistureMax == null) {
            binding.etFormMoistureMax.error = getString(R.string.plant_moisture_invalid_error)
            binding.etFormMoistureMax.requestFocus()
            return
        }
        if (moistureMin > moistureMax) {
            binding.etFormMoistureMin.error = getString(R.string.plant_moisture_range_invalid_error)
            binding.etFormMoistureMin.requestFocus()
            return
        }

        val categoryLabel = binding.actFormCategory.text.toString()
        val category = PlantCategory.values().find { PlantCategoryStyle.categoryLabel(it) == categoryLabel }
            ?: PlantCategory.HERB

        val existingPlant = currentDetailPlant.takeIf { it?.id == editingPlantId }
        val photoPath = pickedPhotoPath ?: existingPlant?.photoPath

        val plant = Plant(
            id = editingPlantId ?: customPlantRepo.newId(),
            thaiName = name,
            scientificName = binding.etFormScientific.text.toString().trim(),
            category = category,
            photoPath = photoPath,
            isCustom = true,
            description = binding.etFormDescription.text.toString().trim(),
            phMin = phMin,
            phMax = phMax,
            moistureMin = moistureMin,
            moistureMax = moistureMax,
            fertilizerAdvice = binding.etFormFertilizer.text.toString().trim(),
            references = emptyList()
        )

        // เช็คผลจริงว่าบันทึกลงเครื่องสำเร็จไหม ไม่ใช่แค่สมมติว่าสำเร็จเสมอ
        val success = if (editingPlantId != null) {
            customPlantRepo.update(plant)
        } else {
            customPlantRepo.add(plant)
        }

        if (success) {
            Toast.makeText(requireContext(), getString(R.string.plant_saved_toast), Toast.LENGTH_SHORT).show()
            refreshList()
            showDetail(plant)
        } else {
            // บันทึกไม่สำเร็จ — อยู่หน้าฟอร์มเดิมไว้ ไม่เสียข้อมูลที่กรอก ให้ผู้ใช้กดลองใหม่ได้ทันที
            showSaveErrorSnackbar(getString(R.string.plant_save_failed))
        }
    }

    private fun showSaveErrorSnackbar(message: String) {
        com.google.android.material.snackbar.Snackbar.make(binding.root, message, com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.rust))
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.paper))
            .show()
    }

    private fun confirmDelete(plant: Plant) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_plant_confirm_title)
            .setMessage(R.string.delete_plant_confirm_message)
            .setPositiveButton(R.string.delete_plant) { _, _ ->
                val success = customPlantRepo.delete(plant.id)
                if (success) {
                    Toast.makeText(requireContext(), getString(R.string.plant_deleted_toast), Toast.LENGTH_SHORT).show()
                    refreshList()
                    showList()
                } else {
                    // ลบไม่สำเร็จ — อยู่หน้าเดิมไว้ ไม่ต้องพาออกไปหน้า list เพราะข้อมูลอาจยังไม่ถูกลบจริง
                    showSaveErrorSnackbar(getString(R.string.plant_delete_failed))
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    // ==================== List screen ====================

    private fun showList() {
        currentScreen = PlantsScreen.LIST
        binding.plantDetailContainer.visibility = View.GONE
        binding.plantFormContainer.visibility = View.GONE
        binding.plantListContainer.visibility = View.VISIBLE
        fadeIn(binding.plantListContainer)
        binding.fabAddPlant.visibility = View.VISIBLE
        backCallback.isEnabled = false
        refreshList(binding.etSearchPlant.text.toString())
    }

    /** เฟดเข้า + เลื่อนขึ้นเบาๆ ตอนสลับหน้าจอ (list/detail/form)
     *  ใช้แค่ alpha/translationY ซึ่ง GPU จัดการได้ทั้งคู่ ไม่กินซีพียูเพิ่ม เล่นครั้งเดียวจบ ไม่มี loop ค้าง */
    private fun fadeIn(view: View) {
        view.alpha = 0f
        view.translationY = 18f
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(240)
            .setInterpolator(AnimUtils.emphasized())
            .start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
