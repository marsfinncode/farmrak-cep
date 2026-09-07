package com.farmpulse.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.farmpulse.app.MainActivity
import com.farmpulse.app.R
import com.farmpulse.app.data.PhStatus
import com.farmpulse.app.data.SensorData
import com.farmpulse.app.data.UserProfileRepository
import com.farmpulse.app.databinding.DialogConnectionHelpBinding
import com.farmpulse.app.databinding.FragmentDashboardBinding
import com.farmpulse.app.network.ConnectionState
import com.farmpulse.app.viewmodel.CommandAction
import com.farmpulse.app.viewmodel.CommandFeedback
import com.farmpulse.app.viewmodel.SensorViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    // ใช้ ViewModel instance เดียวกับ Activity (สร้างครั้งเดียว) เพื่อรับ "ตัวแปรจาก ESP32" ที่มาจาก MQTT
    // และให้ทุกแท็บเห็นข้อมูลชุดเดียวกันแบบ sync กันเสมอ
    private val viewModel: SensorViewModel by lazy { (requireActivity() as MainActivity).sensorViewModel }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val profileRepo = UserProfileRepository(requireContext())
        binding.tvGreeting.text = "สวัสดี คุณ${profileRepo.displayName()}"

        binding.btnRefresh.setOnClickListener { viewModel.connectToBoard() }
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.connectToBoard()
            binding.swipeRefresh.isRefreshing = false
        }

        binding.switchAutoMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleAutoMode(isChecked)
            updateManualControlsEnabled(isChecked)
            viewModel.sensorData.value?.let { current ->
                if (current.soilMoisture < viewModel.currentSettings().moistureThreshold) {
                    binding.tvMoistureDesc.text = if (isChecked) "ดินแห้ง กำลังรดน้ำให้อัตโนมัติ" else "ดินแห้ง โปรดสั่งการรดน้ำ"
                }
            }
        }

        binding.btnWater.setOnClickListener {
            val turningOn = binding.btnWater.text == getString(R.string.water_now)
            viewModel.setPump(turningOn)
        }
        binding.btnFertilizer.setOnClickListener {
            showFertilizerComingSoonDialog()
        }
        binding.tvFertChip.setOnClickListener {
            showFertilizerComingSoonDialog()
        }

        binding.btnConnectionHelp.setOnClickListener { showConnectionHelpDialog() }
        binding.btnSyncTime.setOnClickListener { syncBoardTimeNow() }

        viewModel.sensorData.observe(viewLifecycleOwner) { data -> renderData(data) }
        viewModel.connectionState.observe(viewLifecycleOwner) { state -> renderConnectionState(state) }
        viewModel.commandFeedback.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { feedback -> showCommandFeedback(feedback) }
        }
    }

    private fun showFertilizerComingSoonDialog() {
        val dialogBinding = com.farmpulse.app.databinding.DialogFertilizerComingSoonBinding.inflate(layoutInflater)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(dialogBinding.root)
        dialogBinding.btnGotItFertilizer.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showCommandFeedback(feedback: CommandFeedback) {
        if (!isAdded || _binding == null) return
        val currentContext = context ?: return
        val message = when (feedback.action) {
            CommandAction.WATER -> if (feedback.success) getString(R.string.command_water_success) else getString(R.string.command_water_fail)
            CommandAction.FERTILIZER -> if (feedback.success) getString(R.string.command_fert_success) else getString(R.string.command_fert_fail)
        }
        val bgColor = if (feedback.success) R.color.leaf else R.color.rust
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).apply {
            setBackgroundTint(ContextCompat.getColor(currentContext, bgColor))
            setTextColor(ContextCompat.getColor(currentContext, R.color.paper))
        }.show()
    }

    private fun showConnectionHelpDialog() {
        val dialogBinding = DialogConnectionHelpBinding.inflate(layoutInflater)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(dialogBinding.root)
        dialogBinding.btnGotIt.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun renderData(data: SensorData) {
        val inkColor = ContextCompat.getColor(requireContext(), R.color.ink)

        binding.tvMoistureValue.text = "${data.soilMoisture.toInt()}%"
        binding.tvMoistureValue.setTextColor(inkColor)
        binding.tvMoistureDesc.text = if (data.soilMoisture < viewModel.currentSettings().moistureThreshold) {
            if (data.autoMode) "ดินแห้ง กำลังรดน้ำให้อัตโนมัติ" else "ดินแห้ง โปรดสั่งการรดน้ำ"
        } else {
            "พอดีแล้ว ยังไม่ต้องรดน้ำ"
        }

        binding.tvPhValue.text = String.format(Locale.US, "%.1f", data.phValue)

        // เปลี่ยนสีตัวเลข pH ตามสถานะ ให้เห็นความผิดปกติได้ทันทีโดยไม่ต้องอ่านข้อความ
        // (pH เป็นค่าที่ไม่มีระบบใดคอยแก้ให้อัตโนมัติ ต่างจากความชื้นที่ปั๊มจัดการเอง จึงต้องเน้นให้สังเกตง่าย)
        val settings = viewModel.currentSettings()
        val phStatus = data.phStatusLabel(settings.phMin, settings.phMax)
        val phColorRes = when (phStatus) {
            PhStatus.OK -> R.color.ink
            PhStatus.TOO_ACIDIC -> R.color.rust
            PhStatus.TOO_ALKALINE -> R.color.amber_text
        }
        val phColor = ContextCompat.getColor(requireContext(), phColorRes)
        binding.tvPhValue.setTextColor(phColor)

        binding.tvPhDesc.text = when (phStatus) {
            PhStatus.OK -> "เหมาะสมกับการปลูกพืชแล้ว"
            PhStatus.TOO_ACIDIC -> "เป็นกรดเกินไป ควรตรวจสอบ"
            PhStatus.TOO_ALKALINE -> "เป็นด่างเกินไป ควรตรวจสอบ"
        }
        // ข้อความอธิบายก็เปลี่ยนสีตามกัน (สีเทาปกติเมื่อค่าอยู่ในเกณฑ์)
        binding.tvPhDesc.setTextColor(
            if (phStatus == PhStatus.OK) ContextCompat.getColor(requireContext(), R.color.ink_soft) else phColor
        )

        binding.tvPumpStatus.text = if (data.pumpStatus) "กำลังรดน้ำ" else getString(R.string.status_off)
        binding.tvPumpStatus.setTextColor(inkColor)
        binding.tvFertStatus.text = "เร็วๆ นี้"
        binding.tvFertStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.amber_text))

        binding.switchAutoMode.setOnCheckedChangeListener(null)
        binding.switchAutoMode.isChecked = data.autoMode
        binding.switchAutoMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleAutoMode(isChecked)
            updateManualControlsEnabled(isChecked)
            viewModel.sensorData.value?.let { current ->
                if (current.soilMoisture < viewModel.currentSettings().moistureThreshold) {
                    binding.tvMoistureDesc.text = if (isChecked) "ดินแห้ง กำลังรดน้ำให้อัตโนมัติ" else "ดินแห้ง โปรดสั่งการรดน้ำ"
                }
            }
        }
        updateManualControlsEnabled(data.autoMode)

        binding.btnWater.text = if (data.pumpStatus) getString(R.string.stop_water) else getString(R.string.water_now)
        binding.btnFertilizer.text = getString(R.string.fert_now)

        // แสดง "เวลาบนบอร์ด" ให้ชัดเจน (มาจาก timestamp จริงที่บอร์ด sync ไว้) พร้อมเทียบกับเวลามือถือ
        // ถ้าต่างกันเกิน 5 วินาที จะเปลี่ยนเป็นสีส้มเตือนให้กดปุ่ม "ซิงค์เวลา"
        updateBoardTimeDisplay(data.timestampMillis)
    }

    private fun updateBoardTimeDisplay(boardTimestampMillis: Long) {
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(boardTimestampMillis))
        binding.tvBoardTime.text = getString(R.string.board_time_label, timeStr)

        val diffSeconds = kotlin.math.abs(System.currentTimeMillis() - boardTimestampMillis) / 1000
        val mismatch = diffSeconds > 5
        binding.tvBoardTime.setTextColor(
            ContextCompat.getColor(requireContext(), if (mismatch) R.color.amber_text else R.color.ink_soft)
        )
        binding.btnSyncTime.visibility = View.VISIBLE
    }

    private fun syncBoardTimeNow() {
        viewModel.syncBoardTime { success ->
            activity?.runOnUiThread {
                if (_binding == null) return@runOnUiThread
                val message = if (success) getString(R.string.sync_time_success) else getString(R.string.sync_time_failed)
                val color = if (success) R.color.leaf else R.color.rust
                Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).apply {
                    setBackgroundTint(ContextCompat.getColor(requireContext(), color))
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.paper))
                }.show()
            }
        }
    }

    private fun renderConnectionState(state: ConnectionState) {
        binding.tvConnectionStatus.text = when (state) {
            ConnectionState.CONNECTED -> getString(R.string.connected)
            ConnectionState.CONNECTING -> getString(R.string.connecting)
            ConnectionState.RECONNECTING -> getString(R.string.reconnecting)
            ConnectionState.ERROR -> "เชื่อมต่อไม่สำเร็จ ลองใหม่อีกครั้ง"
            ConnectionState.DISCONNECTED -> getString(R.string.not_connected)
        }

        val dotColor = when (state) {
            ConnectionState.CONNECTED -> R.color.leaf
            ConnectionState.CONNECTING, ConnectionState.RECONNECTING -> R.color.amber
            ConnectionState.ERROR, ConnectionState.DISCONNECTED -> R.color.rust
        }
        binding.dotConnectionStatus.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), dotColor)

        // แสดงปุ่ม "?" ให้กดดูวิธีเชื่อมต่อ เฉพาะตอนที่ไม่ได้เชื่อมต่อจริงๆ (ไม่ใช่ตอนกำลังเชื่อมต่อครั้งแรกหรือเน็ตสะดุดชั่วคราว)
        val showHelp = state == ConnectionState.DISCONNECTED || state == ConnectionState.ERROR
        binding.btnConnectionHelp.visibility = if (showHelp) View.VISIBLE else View.GONE

        // แสดง placeholder เฉพาะเมื่อยังไม่เคยมีข้อมูล หรือตัดการเชื่อมต่อ/error ชัดเจน
        // หากอยู่ในสถานะ CONNECTING หรือ RECONNECTING และมีข้อมูลเดิมอยู่แล้ว จะยังคงแสดงค่าเดิมไว้ไม่ให้จอกระพริบ
        if (state == ConnectionState.DISCONNECTED || (state == ConnectionState.ERROR && viewModel.sensorData.value == null)) {
            showNotConnectedPlaceholders()
        }
    }

    private fun showNotConnectedPlaceholders() {
        val rustColor = ContextCompat.getColor(requireContext(), R.color.rust)
        val amberColor = ContextCompat.getColor(requireContext(), R.color.amber_text)

        binding.tvMoistureValue.text = "--"
        binding.tvMoistureValue.setTextColor(rustColor)
        binding.tvMoistureDesc.text = getString(R.string.no_data_yet)

        binding.tvPhValue.text = "--"
        binding.tvPhValue.setTextColor(rustColor)
        binding.tvPhDesc.text = getString(R.string.no_data_yet)
        // รีเซ็ตสีข้อความ pH กลับเป็นปกติ กันสีแดง/ส้มจากค่าก่อนหน้าค้างอยู่ตอนขาดการเชื่อมต่อ
        binding.tvPhDesc.setTextColor(ContextCompat.getColor(requireContext(), R.color.ink_soft))

        binding.tvPumpStatus.text = getString(R.string.status_not_connected)
        binding.tvPumpStatus.setTextColor(amberColor)
        binding.tvFertStatus.text = "เร็วๆ นี้"
        binding.tvFertStatus.setTextColor(amberColor)

        binding.tvBoardTime.text = ""
        binding.btnSyncTime.visibility = View.GONE
    }

    private fun updateManualControlsEnabled(autoMode: Boolean) {
        binding.btnWater.isEnabled = !autoMode
        binding.btnWater.alpha = if (autoMode) 0.45f else 1f
        // ปุ่มให้ปุ๋ยสามารถกดเพื่อดูหน้าต่างแผนพัฒนาได้ตลอด
        binding.btnFertilizer.isEnabled = true
        binding.btnFertilizer.alpha = 1f
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
