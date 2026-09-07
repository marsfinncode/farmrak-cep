package com.farmpulse.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.farmpulse.app.MainActivity
import com.farmpulse.app.R
import android.widget.Toast
import com.farmpulse.app.databinding.FragmentHistoryBinding
import com.farmpulse.app.viewmodel.SensorViewModel

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SensorViewModel by lazy { (requireActivity() as MainActivity).sensorViewModel }
    private val logAdapter = LogAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.sparklineMoisture.lineColor = ContextCompat.getColor(requireContext(), R.color.water)
        binding.sparklinePh.lineColor = ContextCompat.getColor(requireContext(), R.color.leaf)

        binding.recyclerLogs.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerLogs.adapter = logAdapter

        viewModel.moistureHistory.observe(viewLifecycleOwner) { binding.sparklineMoisture.setPoints(it) }
        viewModel.phHistory.observe(viewLifecycleOwner) { binding.sparklinePh.setPoints(it) }
        viewModel.logs.observe(viewLifecycleOwner) { logs ->
            logAdapter.submitList(logs)
            binding.tvNoLogs.visibility = if (logs.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerLogs.visibility = if (logs.isEmpty()) View.GONE else View.VISIBLE
        }

        binding.btnClearLog.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.clear_log_confirm_title)
                .setMessage(R.string.clear_log_confirm_message)
                .setPositiveButton(R.string.clear_log) { _, _ ->
                    viewModel.clearLogs()
                    Toast.makeText(requireContext(), getString(R.string.log_cleared_toast), Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
