package com.farmpulse.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.farmpulse.app.data.UserProfileRepository
import com.farmpulse.app.databinding.ActivityOnboardingBinding
import com.farmpulse.app.util.ImageStorageUtil
import java.io.File

/**
 * แสดงครั้งแรกที่เปิดแอปเท่านั้น (หลังหน้าอินโทร) — เก็บชื่อ/อาชีพ/ปีประสบการณ์/รูปโปรไฟล์
 * ผู้ใช้แก้ไขข้อมูลนี้ภายหลังได้ที่หน้าตั้งค่า
 */
class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var profileRepo: UserProfileRepository
    private var pickedImagePath: String? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            val path = ImageStorageUtil.copyToInternalStorage(this, uri)
            if (path != null) {
                pickedImagePath = path
                binding.imgProfile.setImageURI(Uri.fromFile(File(path)))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        profileRepo = UserProfileRepository(applicationContext)

        val openPicker = {
            pickImage.launch(
                PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    .build()
            )
        }
        binding.imgProfile.setOnClickListener { openPicker() }
        binding.tvTapPhoto.setOnClickListener { openPicker() }

        // Dropdown แทนช่องพิมพ์อิสระ — เลือกจากตัวเลือกที่กำหนดไว้ล่วงหน้าเท่านั้น
        binding.actOccupation.setAdapter(
            ArrayAdapter(this, R.layout.item_dropdown, resources.getStringArray(R.array.occupation_options))
        )
        binding.actExperience.setAdapter(
            ArrayAdapter(this, R.layout.item_dropdown, resources.getStringArray(R.array.experience_options))
        )

        binding.btnStart.setOnClickListener { saveAndContinue() }
        binding.tvSkip.setOnClickListener {
            profileRepo.onboardingCompleted = true
            askUsedBeforeThenContinue()
        }
    }

    private fun saveAndContinue() {
        val name = binding.etName.text.toString().trim()
        if (name.isEmpty()) {
            binding.etName.error = getString(R.string.name_required_error)
            return
        }
        try {
            profileRepo.userName = name
            profileRepo.occupation = binding.actOccupation.text.toString().trim()
            profileRepo.farmingExperience = binding.actExperience.text.toString().trim()
            pickedImagePath?.let { profileRepo.profileImagePath = it }
            profileRepo.onboardingCompleted = true
            askUsedBeforeThenContinue()
        } catch (e: Exception) {
            // บันทึกไม่สำเร็จ — อยู่หน้าเดิมไว้ ไม่พาไปหน้าหลัก กันข้อมูลที่กรอกหาย
            android.widget.Toast.makeText(this, getString(R.string.profile_save_failed), android.widget.Toast.LENGTH_LONG).show()
        }
    }

    /** ถามว่าเคยใช้แอปมาก่อนไหม — ถ้ายังไม่เคย พาเข้าหน้าหลักพร้อมเริ่ม tutorial ทันที */
    private fun askUsedBeforeThenContinue() {
        AlertDialog.Builder(this)
            .setTitle(R.string.tutorial_used_before_title)
            .setMessage(R.string.tutorial_used_before_message)
            .setCancelable(false)
            .setPositiveButton(R.string.tutorial_used_before_no) { _, _ -> goToMain(startTutorial = true) }
            .setNegativeButton(R.string.tutorial_used_before_yes) { _, _ -> goToMain(startTutorial = false) }
            .show()
    }

    private fun goToMain(startTutorial: Boolean) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_START_TUTORIAL, startTutorial)
        startActivity(intent)
        finish()
    }
}
