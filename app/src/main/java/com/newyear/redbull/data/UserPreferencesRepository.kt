package com.newyear.redbull.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.newyear.redbull.model.BasicFunctionalityState
import com.newyear.redbull.model.ExperimentalFunctionalityState
import com.newyear.redbull.model.MonitorOptionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserPreferencesRepository (
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        const val TAG = "UserPreferencesRepository"
        // Basic functionality
        val AUTO_OPEN_RED_PACKET = booleanPreferencesKey("auto_open_packet")
        val DELAY_OPEN_RED_PACKET = intPreferencesKey("delay_open_packet")
        val DELAY_CLOSE_RED_PACKET = intPreferencesKey("delay_close_packet")
        val OPEN_RED_PACKET_YOURSELF = booleanPreferencesKey("open_packet_yourself")
        val SHIELD_RED_PACKET_TEXT = stringPreferencesKey("shield_packet_text")
        // Monitor options
        val MONITOR_SYSTEM_NOTIFICATION = booleanPreferencesKey("monitor_system_notification")
        val MONITOR_CHAT_LIST = booleanPreferencesKey("monitor_chat_list")
        // Experimental functionality
        val OPEN_PACKET_IN_BREATH_MODE = booleanPreferencesKey("open_packet_in_breath_mode")
    }

    suspend fun saveBasicFunctionalityPreference(state: BasicFunctionalityState) {
        dataStore.edit { preferences ->
            preferences[AUTO_OPEN_RED_PACKET] = state.autoOpenRedPacket
            preferences[DELAY_OPEN_RED_PACKET] = state.delaySeconds
            preferences[DELAY_CLOSE_RED_PACKET] = state.delayCloseSeconds
            preferences[OPEN_RED_PACKET_YOURSELF] = state.openRedPacketMySelf
            preferences[SHIELD_RED_PACKET_TEXT] = state.shieldTextContent
        }
    }

    suspend fun saveMonitorOptionPreference(state: MonitorOptionState) {
        dataStore.edit { preference ->
            preference[MONITOR_SYSTEM_NOTIFICATION] = state.monitorSystemNotification
            preference[MONITOR_CHAT_LIST] = state.monitorChatListNotification
        }
    }

    suspend fun saveExperimentalFunctionalityPreference(state: ExperimentalFunctionalityState) {
        dataStore.edit { preference ->
            preference[OPEN_PACKET_IN_BREATH_MODE] = state.openReadPacketInBreathMode
        }
    }

    val basicFunctionalityPreferences: Flow<BasicFunctionalityState> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preference ->
        BasicFunctionalityState(
            autoOpenRedPacket = preference[AUTO_OPEN_RED_PACKET] ?: true,
            delaySeconds = preference[DELAY_OPEN_RED_PACKET] ?: 200,
            openRedPacketMySelf = preference[OPEN_RED_PACKET_YOURSELF] ?: false,
            shieldTextContent = preference[SHIELD_RED_PACKET_TEXT] ?: ""
        )
    }

    val monitorOptionPreference: Flow<MonitorOptionState> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preference ->
            MonitorOptionState(
                monitorSystemNotification = preference[MONITOR_SYSTEM_NOTIFICATION] ?: true,
                monitorChatListNotification = preference[MONITOR_CHAT_LIST] ?: true
            )
        }

    val experimentalFunctionalityPreferences: Flow<ExperimentalFunctionalityState> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preference ->
            ExperimentalFunctionalityState(
                openReadPacketInBreathMode = preference[OPEN_PACKET_IN_BREATH_MODE] ?: false
            )
        }

    val delayOpenSeconds: Flow<Int> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preference ->
            preference[DELAY_OPEN_RED_PACKET] ?: 200
        }

    val delayCloseSeconds: Flow<Int> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preference ->
            preference[DELAY_CLOSE_RED_PACKET] ?: 0
        }
}