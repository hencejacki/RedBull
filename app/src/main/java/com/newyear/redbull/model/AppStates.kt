package com.newyear.redbull.model

data class BasicFunctionalityState(
    val autoOpenRedPacket: Boolean = true,
    val delaySeconds: Int = 200,
    val delayCloseSeconds: Int = 0,
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