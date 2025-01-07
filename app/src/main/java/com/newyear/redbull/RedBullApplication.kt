package com.newyear.redbull

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.newyear.redbull.data.UserPreferencesRepository

private const val USER_PREFERENCES_NAME = "user_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = USER_PREFERENCES_NAME
)

class RedBullApplication : Application() {

    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate() {
        super.onCreate()
        userPreferencesRepository = UserPreferencesRepository(dataStore)
    }
}