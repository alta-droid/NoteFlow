package com.noteflow.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noteflow.app.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val apiKey: StateFlow<String> = settingsRepository.apiKeyFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val appTheme: StateFlow<String> = settingsRepository.appThemeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    fun saveApiKey(key: String) {
        viewModelScope.launch {
            settingsRepository.setApiKey(key)
        }
    }

    fun saveAppTheme(theme: String) {
        viewModelScope.launch {
            settingsRepository.setAppTheme(theme)
        }
    }
}
