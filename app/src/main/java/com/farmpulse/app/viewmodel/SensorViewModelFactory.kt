package com.farmpulse.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.farmpulse.app.data.SettingsRepository
import com.farmpulse.app.notification.NotificationHelper

class SensorViewModelFactory(
    private val settings: SettingsRepository,
    private val notificationHelper: NotificationHelper
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SensorViewModel(settings, notificationHelper) as T
    }
}
