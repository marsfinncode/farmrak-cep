package com.farmpulse.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.farmpulse.app.MainActivity
import com.farmpulse.app.R
import com.farmpulse.app.data.AboutContent
import com.farmpulse.app.data.CustomPlantRepository
import com.farmpulse.app.data.FertilizerSchedule
import com.farmpulse.app.data.FertilizerScheduleRepository
import com.farmpulse.app.data.Plant
import com.farmpulse.app.data.PlantData
import com.farmpulse.app.data.UserProfileRepository
import com.farmpulse.app.databinding.DialogAddFertilizerScheduleBinding
import com.farmpulse.app.databinding.FragmentSettingsBinding
import com.farmpulse.app.databinding.ItemAboutStatBinding
import com.farmpulse.app.databinding.ItemFertilizerScheduleBinding
import com.farmpulse.app.util.AnimUtils
import com.farmpulse.app.util.ImageStorageUtil
import com.farmpulse.app.viewmodel.SensorViewModel
import java.io.File

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SensorViewModel by lazy { (requireActivity() as MainActivity).sensorViewModel }
    private lateinit var profileRepo: UserProfileRepository
    private lateinit var scheduleRepo: FertilizerScheduleRepository
    private var pickedProfileImagePath: String? = null

    // ต้อง register ก่อน fragment เข้าสถานะ CREATED เลยประกาศเป็น property ตรงนี้
    private val pickProfileImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            val path = ImageStorageUtil.copyToInternalStorage(requireContext(), uri)
            if (path != null) {
                pickedProfileImagePath = path
                binding.imgProfileSettings.setImageURI(Uri.fromFile(File(path)))
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val settings = viewModel.currentSettings()
        profileRepo = UserProfileRepository(requireContext())
        scheduleRepo = FertilizerScheduleRepository(requireContext())

        // --- โปรไฟล์ผู้ใช้ ---
        binding.etProfileName.setText(profileRepo.userName)

        binding.actProfileOccupation.setAdapter(
            ArrayAdapter(requireContext(), R.layout.item_dropdown, resources.getStringArray(R.array.occupation_options))
        )
        binding.actProfileExperience.setAdapter(
            ArrayAdapter(requireContext(), R.layout.item_dropdown, resources.getStringArray(R.array.experience_options))
        )
        if (profileRepo.occupation.isNotBlank()) binding.actProfileOccupation.setText(profileRepo.occupation, false)
        if (profileRepo.farmingExperience.isNotBlank()) binding.actProfileExperience.setText(profileRepo.farmingExperience, false)

        profileRepo.profileImagePath?.let { path ->
            val file = File(path)
            if (file.exists()) binding.imgProfileSettings.setImageURI(Uri.fromFile(file))
        }

        val openProfilePicker = {
            pickProfileImage.launch(
                PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    .build()
            )
        }
        binding.imgProfileSettings.setOnClickListener { openProfilePicker() }

        binding.btnSaveProfile.setOnClickListener {
            try {
                profileRepo.userName = binding.etProfileName.text.toString().trim()
                profileRepo.occupation = binding.actProfileOccupation.text.toString().trim()
                profileRepo.farmingExperience = binding.actProfileExperience.text.toString().trim()
                pickedProfileImagePath?.let { profileRepo.profileImagePath = it }
                Toast.makeText(requireContext(), getString(R.string.profile_saved_toast), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                showSettingsErrorSnackbar(getString(R.string.profile_save_failed))
            }
        }

        // --- การเชื่อมต่อกับบอร์ด / เกณฑ์อัตโนมัติ ---
        binding.etBoardIp.setText(settings.boardIpAddress)
        binding.seekMoistureThreshold.progress = settings.moistureThreshold
        // SeekBar เก็บ pH เป็นจำนวนเต็ม 0-140 แล้วหารสิบตอนใช้งานจริง เพื่อให้ลาก slider ได้ละเอียดถึงทศนิยม 1 ตำแหน่ง
        binding.seekPhMin.progress = (settings.phMin * 10).toInt()
        binding.seekPhMax.progress = (settings.phMax * 10).toInt()

        updateLabels()

        val listener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateLabels()
                // ถ้าผู้ใช้เป็นคนลากเอง (ไม่ใช่โค้ดตั้งค่าให้) แปลว่าไม่ได้ใช้ค่าตามพืชแล้ว -> สลับเป็น "กำหนดเอง"
                if (fromUser) switchToCustomPreset()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }
        binding.seekMoistureThreshold.setOnSeekBarChangeListener(listener)
        binding.seekPhMin.setOnSeekBarChangeListener(listener)
        binding.seekPhMax.setOnSeekBarChangeListener(listener)

        setupPlantPresetDropdown()

        // แสดงว่าตอนนี้แอปคุยกับบอร์ดผ่าน IP ไหน (ระบบค้นหาให้เองอัตโนมัติ)
        viewModel.activeBoardIp.observe(viewLifecycleOwner) { ip ->
            binding.tvActiveIp.text = if (ip.isNullOrBlank())
                getString(R.string.board_searching)
            else
                getString(R.string.board_connected_via, ip)
            binding.tvActiveIp.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (ip.isNullOrBlank()) R.color.ink_soft else R.color.leaf_deep
                )
            )
        }

        binding.btnSaveThresholds.setOnClickListener {
            try {
                val moisture = binding.seekMoistureThreshold.progress
                val phMin = binding.seekPhMin.progress / 10f
                val phMax = binding.seekPhMax.progress / 10f

                viewModel.updateThresholds(moisture, phMin, phMax)
                // บันทึกพืชที่เลือกไว้พร้อมกัน เพื่อให้ชื่อพืชกับค่าจริงตรงกันเสมอ
                pendingPresetId?.let { viewModel.currentSettings().plantPresetId = it }
                pendingPresetId = null
                Toast.makeText(requireContext(), getString(R.string.saved_toast), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                showSettingsErrorSnackbar(getString(R.string.settings_save_failed))
            }
        }

        binding.btnSaveBoardIp.setOnClickListener {
            binding.etBoardIp.error = null
            // ปล่อยว่างได้แล้ว เพราะระบบค้นหาบอร์ดอัตโนมัติจะจัดการให้เอง
            val boardIp = binding.etBoardIp.text.toString().trim()
            try {
                viewModel.updateBoardIp(boardIp)
                Toast.makeText(requireContext(), getString(R.string.saved_toast), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                showSettingsErrorSnackbar(getString(R.string.settings_save_failed))
            }
        }

        binding.btnSaveBoardWifi.setOnClickListener {
            binding.etBoardWifiSsid.error = null
            val ssid = binding.etBoardWifiSsid.text.toString().trim()
            if (ssid.isEmpty()) {
                binding.etBoardWifiSsid.error = getString(R.string.board_wifi_ssid_required_error)
                binding.etBoardWifiSsid.requestFocus()
                return@setOnClickListener
            }
            val password = binding.etBoardWifiPassword.text.toString()

            binding.tvBoardWifiStatus.text = getString(R.string.board_wifi_saving)
            viewModel.sendBoardWifiConfig(ssid, password) { success ->
                activity?.runOnUiThread {
                    if (success) {
                        // รอสักครู่ให้บอร์ดลองเชื่อมต่อจริงก่อน แล้วค่อยเช็คสถานะ
                        binding.root.postDelayed({ checkBoardWifiStatus() }, 3000)
                    } else {
                        binding.tvBoardWifiStatus.text = getString(R.string.board_wifi_save_failed)
                    }
                }
            }
        }
        checkBoardWifiStatus()

        // --- เกี่ยวกับแอป ---
        binding.tvProblemText.text = AboutContent.problemText
        binding.tvGapText.text = AboutContent.gapText
        binding.tvSolutionText.text = AboutContent.solutionText
        binding.tvProgramText.text = AboutContent.programText
        binding.tvTeamMembers.text = AboutContent.teamMembers.joinToString("\n") { "•  $it" }
        binding.tvAdvisors.text = AboutContent.advisors.joinToString("\n") { "•  $it" }

        // การ์ดตัวเลขสำคัญ — ดึงสถิติออกมาเน้นให้เห็นภาพปัญหาได้เร็ว แทนที่จะซ่อนอยู่ในข้อความยาว
        binding.statsContainer.removeAllViews()
        AboutContent.stats.forEach { stat ->
            val statBinding = ItemAboutStatBinding.inflate(layoutInflater, binding.statsContainer, false)
            statBinding.tvStatNumber.text = stat.number
            statBinding.tvStatCaption.text = stat.caption
            binding.statsContainer.addView(statBinding.root)
        }

        binding.historyReferencesContainer.removeAllViews()
        AboutContent.historyReferences.forEach { ref ->
            val tv = android.widget.TextView(requireContext()).apply {
                text = "•  ${ref.label}"
                setTextColor(ContextCompat.getColor(requireContext(), R.color.water))
                textSize = 11.5f
                setPadding(0, 4, 0, 4)
                setOnClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(ref.url)))
                }
            }
            binding.historyReferencesContainer.addView(tv)
        }

        binding.aboutHeaderRow.setOnClickListener {
            val expanding = binding.aboutContentCard.visibility != View.VISIBLE
            binding.aboutContentCard.visibility = if (expanding) View.VISIBLE else View.GONE
            // ลูกศรค่อยๆ หมุนตามแทนที่จะสลับทันที (การ์ดกางออกเองผ่าน animateLayoutChanges)
            binding.imgAboutChevron.animate()
                .rotation(if (expanding) 270f else 90f)
                .setDuration(AnimUtils.DURATION_MEDIUM)
                .setInterpolator(AnimUtils.emphasized())
                .start()
        }

        binding.rowTutorial.setOnClickListener {
            (requireActivity() as MainActivity).startTutorial()
        }


        // --- ตารางให้ปุ๋ยอัตโนมัติ ---
        binding.btnAddSchedule.setOnClickListener { showAddScheduleDialog() }
        refreshScheduleList()
    }

    private fun refreshScheduleList() {
        val schedules = scheduleRepo.getAll()
        binding.scheduleListContainer.removeAllViews()
        binding.tvNoSchedules.visibility = if (schedules.isEmpty()) View.VISIBLE else View.GONE

        schedules.forEach { schedule ->
            val itemBinding = ItemFertilizerScheduleBinding.inflate(layoutInflater, binding.scheduleListContainer, false)
            itemBinding.tvScheduleTime.text = "${schedule.displayTime()} น."
            itemBinding.tvScheduleDuration.text = getString(R.string.fert_schedule_duration_format, schedule.durationMinutes)
            itemBinding.btnDeleteSchedule.setOnClickListener {
                scheduleRepo.delete(schedule.id)
                refreshScheduleList()
                pushScheduleToBoard(getString(R.string.fert_schedule_deleted_toast))
            }
            binding.scheduleListContainer.addView(itemBinding.root)
        }
    }

    private fun showAddScheduleDialog() {
        val dialogBinding = DialogAddFertilizerScheduleBinding.inflate(layoutInflater)
        dialogBinding.timePickerFert.setIs24HourView(true)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancelSchedule.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnSaveSchedule.setOnClickListener {
            dialogBinding.etFertDuration.error = null
            val duration = dialogBinding.etFertDuration.text.toString().toIntOrNull()
            if (duration == null || duration <= 0) {
                dialogBinding.etFertDuration.error = getString(R.string.fert_duration_required_error)
                return@setOnClickListener
            }

            val hour: Int
            val minute: Int
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                hour = dialogBinding.timePickerFert.hour
                minute = dialogBinding.timePickerFert.minute
            } else {
                @Suppress("DEPRECATION")
                hour = dialogBinding.timePickerFert.currentHour
                @Suppress("DEPRECATION")
                minute = dialogBinding.timePickerFert.currentMinute
            }

            scheduleRepo.add(FertilizerSchedule(scheduleRepo.newId(), hour, minute, duration))
            refreshScheduleList()
            dialog.dismiss()
            pushScheduleToBoard(getString(R.string.fert_schedule_saved_toast))
        }
        dialog.show()
    }

    /** ส่งตารางทั้งหมดไปให้บอร์ดเก็บไว้เอง — สำคัญมาก เพราะบอร์ดต้องให้ปุ๋ยตามเวลาได้เองแม้ปิดแอป/ตัด WiFi ไปแล้ว */
    private fun pushScheduleToBoard(successMessage: String) {
        viewModel.sendFertilizerSchedule(scheduleRepo.getAll()) { success ->
            activity?.runOnUiThread {
                if (_binding == null) return@runOnUiThread
                if (success) {
                    Toast.makeText(requireContext(), successMessage, Toast.LENGTH_SHORT).show()
                } else {
                    showSettingsErrorSnackbar(getString(R.string.fert_schedule_sync_failed))
                }
            }
        }
    }

    // ===== Preset ค่าอัตโนมัติจากตำราพืช =====
    // เลือกพืช -> ตั้งค่าความชื้น/pH ให้ตรงกับพืชนั้นทันที
    // ผู้ใช้ลาก slider เอง -> สลับกลับเป็น "กำหนดเอง" อัตโนมัติ

    /** กันไม่ให้การตั้งค่า slider ด้วยโค้ด ไปกระตุก listener จนสลับเป็น "กำหนดเอง" เองโดยไม่ตั้งใจ */
    private var applyingPreset = false

    /** preset ที่เลือกไว้แต่ยังไม่ได้กดบันทึก — เก็บลงเครื่องจริงพร้อมกับค่าเกณฑ์ตอนกดปุ่มบันทึกเท่านั้น
     *  (กันกรณีเลือกพืชแล้วออกจากหน้าไปเลย จะได้ไม่เกิดสภาพ "ชื่อพืชบอกอย่าง ค่าจริงเป็นอีกอย่าง") */
    private var pendingPresetId: String? = null

    private fun presetPlants(): List<Plant> = PlantData.all + CustomPlantRepository(requireContext()).getAll()

    private fun setupPlantPresetDropdown() {
        val plants = presetPlants()
        val labels = plants.map { it.thaiName } + getString(R.string.preset_custom)

        binding.actPlantPreset.setAdapter(
            ArrayAdapter(requireContext(), R.layout.item_dropdown, labels)
        )

        // แสดงค่าที่บันทึกไว้ล่าสุด
        val savedId = viewModel.currentSettings().plantPresetId
        val savedPlant = plants.find { it.id == savedId }
        binding.actPlantPreset.setText(
            savedPlant?.thaiName ?: getString(R.string.preset_custom), false
        )
        updatePresetInfo(savedPlant)

        binding.actPlantPreset.setOnItemClickListener { _, _, position, _ ->
            if (position < plants.size) {
                applyPlantPreset(plants[position])
            } else {
                switchToCustomPreset()
            }
        }
    }

    /** ตั้งค่า slider ทั้งสามตามข้อมูลพืชในตำราพืช */
    private fun applyPlantPreset(plant: Plant) {
        applyingPreset = true

        // ใช้ความชื้น "ขั้นต่ำ" ของพืชเป็นเกณฑ์เริ่มรดน้ำ (ต่ำกว่านี้เมื่อไหร่ = ดินแห้งเกินไปสำหรับพืชชนิดนี้)
        binding.seekMoistureThreshold.progress = plant.moistureMin
        binding.seekPhMin.progress = (plant.phMin * 10).toInt()
        binding.seekPhMax.progress = (plant.phMax * 10).toInt()

        applyingPreset = false

        updateLabels()
        pendingPresetId = plant.id
        updatePresetInfo(plant)
    }

    private fun switchToCustomPreset() {
        if (applyingPreset) return
        val currentId = pendingPresetId ?: viewModel.currentSettings().plantPresetId
        if (currentId.isEmpty()) return

        pendingPresetId = ""
        binding.actPlantPreset.setText(getString(R.string.preset_custom), false)
        updatePresetInfo(null)
    }

    private fun updatePresetInfo(plant: Plant?) {
        if (plant == null) {
            binding.tvPresetInfo.text = getString(R.string.preset_custom_info)
            binding.tvPresetInfo.setTextColor(ContextCompat.getColor(requireContext(), R.color.ink_soft))
        } else {
            binding.tvPresetInfo.text = getString(
                R.string.preset_applied, plant.thaiName, plant.moistureMin, plant.phMin, plant.phMax
            )
            binding.tvPresetInfo.setTextColor(ContextCompat.getColor(requireContext(), R.color.leaf_deep))
        }
        binding.tvPresetInfo.visibility = View.VISIBLE
    }

    private fun updateLabels() {
        binding.tvMoistureThreshLabel.text =
            "${getString(R.string.moisture_threshold)}: ${binding.seekMoistureThreshold.progress}%"
        binding.tvPhMinLabel.text =
            "${getString(R.string.ph_min)}: ${binding.seekPhMin.progress / 10f}"
        binding.tvPhMaxLabel.text =
            "${getString(R.string.ph_max)}: ${binding.seekPhMax.progress / 10f}"
    }

    private fun checkBoardWifiStatus() {
        binding.tvBoardWifiStatus.text = getString(R.string.board_wifi_checking)
        viewModel.fetchBoardWifiStatus { status ->
            activity?.runOnUiThread {
                if (_binding == null) return@runOnUiThread
                when {
                    status == null || !status.staConfigured ->
                        binding.tvBoardWifiStatus.text = getString(R.string.board_wifi_none_configured)
                    status.staConnected ->
                        binding.tvBoardWifiStatus.text =
                            getString(R.string.board_wifi_connected, status.staSsid, status.staIp)
                    else ->
                        binding.tvBoardWifiStatus.text =
                            getString(R.string.board_wifi_not_connected_yet, status.staSsid)
                }
            }
        }
    }

    private fun showSettingsErrorSnackbar(message: String) {
        com.google.android.material.snackbar.Snackbar.make(binding.root, message, com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.rust))
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.paper))
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
