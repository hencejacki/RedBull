package com.newyear.redbull.model

data class BasicFunctionalityState(
    val autoOpenRedPacket: Boolean = true,
    val delaySeconds: Float = 0.0F,
    val openRedPacketMySelf: Boolean = false,
    val shieldTextContent: String = ""
)

data class MonitorOptionState(
    val monitorSystemNotification: Boolean = false,
    val monitorChatListNotification: Boolean = false
)

data class ExperimentalFunctionalityState(
    val openReadPacketInBreathMode: Boolean = false
)