package com.newyear.redbull.model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AppViewModel : ViewModel() {
    private var _basicFunctionalityState = MutableStateFlow(BasicFunctionalityState())
    val basicFunctionalityState = _basicFunctionalityState.asStateFlow()

    fun updateAutoOpenRedPaket(value: Boolean) {
        _basicFunctionalityState.update { currentState ->
            currentState.copy(
                autoOpenRedPacket = value
            )
        }
    }

    fun updateDelaySeconds(value: Float) {
        _basicFunctionalityState.update { currentState ->
            currentState.copy(
                delaySeconds = value
            )
        }
    }

    fun updateOpenRedPacketMySelf(value: Boolean) {
        _basicFunctionalityState.update { currentState ->
            currentState.copy(
                openRedPacketMySelf = value
            )
        }
    }

    fun updateShieldTextContent(value: String) {
        _basicFunctionalityState.update { currentState ->
            currentState.copy(
                shieldTextContent = value
            )
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
    }

    fun updateMonitorChatListNotification(value: Boolean) {
        _monitorOptionState.update { currentState ->
            currentState.copy(
                monitorChatListNotification = value
            )
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
    }
}