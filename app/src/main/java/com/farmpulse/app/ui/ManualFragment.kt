package com.farmpulse.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.farmpulse.app.databinding.FragmentManualBinding

class ManualFragment : Fragment() {

    private var _binding: FragmentManualBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManualBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.webViewManual.canGoBack()) {
                    binding.webViewManual.goBack()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })

        binding.webViewManual.settings.javaScriptEnabled = true
        binding.webViewManual.settings.setSupportZoom(true)
        binding.webViewManual.settings.builtInZoomControls = true
        binding.webViewManual.settings.displayZoomControls = false
        
        // บังคับให้โหลดหน้าเว็บแบบซูมออกสุด (เห็นภาพรวมทั้งหน้า)
        binding.webViewManual.settings.loadWithOverviewMode = true
        binding.webViewManual.settings.useWideViewPort = true
        
        binding.webViewManual.webViewClient = WebViewClient()
        binding.webViewManual.webChromeClient = WebChromeClient()
        
        binding.webViewManual.loadUrl("file:///android_asset/FarmRak_Manual.html")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
