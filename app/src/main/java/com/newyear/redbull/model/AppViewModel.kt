package com.newyear.redbull.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.newyear.redbull.RedBullApplication
import com.newyear.redbull.data.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel (
    val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    private var _basicFunctionalityState = MutableStateFlow(BasicFunctionalityState())
    val basicFunctionalityState = _basicFunctionalityState.asStateFlow()

    fun updateAutoOpenRedPaket(value: Boolean) {
        _basicFunctionalityState.update { currentState ->
            currentState.copy(
                autoOpenRedPacket = value
            )
        }
        updateBasicFunctionalityCache()
    }

    fun updateDelaySeconds(value: Int) {
        _basicFunctionalityState.update { currentState ->
            currentState.copy(
                delaySeconds = value
            )
        }
        updateBasicFunctionalityCache()
    }

    fun updateOpenRedPacketMySelf(value: Boolean) {
        _basicFunctionalityState.update { currentState ->
            currentState.copy(
                openRedPacketMySelf = value
            )
        }
        updateBasicFunctionalityCache()
    }

    fun updateShieldTextContent(value: String) {
        _basicFunctionalityState.update { currentState ->
            currentState.copy(
                shieldTextContent = value
            )
        }
        updateBasicFunctionalityCache()
    }

    private fun updateBasicFunctionalityCache() {
        viewModelScope.launch {
            userPreferencesRepository.SaveBasicFunctionalityPreference(basicFunctionalityState.value)
        }
    }

    private var _monitorOptionState = MutableStateFlow(MonitorOptionState())
    val monitorOptionState = _monitorOptionState.asStateFlow()

    fun updateMonitorSystemNotification(value: Boolean) {
        _monitorOptionState.update { currentState ->
            currentState.copy(
                monitorSystemNotification = value
            )
        }
        updateMonitorOptionCache()
    }

    fun updateMonitorChatListNotification(value: Boolean) {
        _monitorOptionState.update { currentState ->
            currentState.copy(
                monitorChatListNotification = value
            )
        }
        updateMonitorOptionCache()
    }

    private fun updateMonitorOptionCache() {
        viewModelScope.launch {
            userPreferencesRepository.SaveMonitorOptionPreference(monitorOptionState.value)
        }
    }

    private var _experimentalFunctionalityState = MutableStateFlow(ExperimentalFunctionalityState())
    val experimentalFunctionalityState = _experimentalFunctionalityState.asStateFlow()

    fun updateOpenRedPacketInBreathMode(value: Boolean) {
        _experimentalFunctionalityState.update { currentState ->
            currentState.copy(
                openReadPacketInBreathMode = value
            )
        }
        updateExperimentalFunctionalityCache()
    }

    private fun updateExperimentalFunctionalityCache() {
        viewModelScope.launch {
            userPreferencesRepository.SaveExperimentalFunctionalityPreference(experimentalFunctionalityState.value)
        }
    }

    init {
        viewModelScope.launch {
            launch {
                userPreferencesRepository.basicFunctionalityPreferences.collectLatest {
                    _basicFunctionalityState.value = it
                }
            }
            launch {
                userPreferencesRepository.monitorOptionPreference.collectLatest {
                    _monitorOptionState.value = it
                }
            }
            launch {
                userPreferencesRepository.experimentalFunctionalityPreferences.collectLatest {
                    _experimentalFunctionalityState.value = it
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as RedBullApplication)
                AppViewModel(application.userPreferencesRepository)
            }
        }
    }
}