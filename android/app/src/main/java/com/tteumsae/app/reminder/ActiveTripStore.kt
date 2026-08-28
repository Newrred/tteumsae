package com.tteumsae.app.reminder

import android.content.Context

interface ActiveTripPreferences {
    fun read(): String?
    fun write(value: String)
    fun clear()
}

class ActiveTripStore(
    private val preferences: ActiveTripPreferences,
) {
    fun save(trip: ActiveTrip) {
        preferences.write(ActiveTripCodec.encode(trip))
    }

    fun loadValid(nowEpochMillis: Long = System.currentTimeMillis()): ActiveTrip? {
        val trip = preferences.read()
            ?.let { runCatching { ActiveTripCodec.decode(it) }.getOrNull() }
        if (trip == null || trip.expiresAtEpochMillis <= nowEpochMillis) {
            preferences.clear()
            return null
        }
        return trip
    }

    fun clear() = preferences.clear()
}

class SharedPreferencesActiveTripPreferences(context: Context) : ActiveTripPreferences {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun read(): String? = preferences.getString(KEY_ACTIVE_TRIP, null)

    override fun write(value: String) {
        preferences.edit().putString(KEY_ACTIVE_TRIP, value).apply()
    }

    override fun clear() {
        preferences.edit().remove(KEY_ACTIVE_TRIP).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "active_trip"
        const val KEY_ACTIVE_TRIP = "snapshot"
    }
}
