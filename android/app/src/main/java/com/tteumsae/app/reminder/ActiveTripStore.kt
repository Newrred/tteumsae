package com.tteumsae.app.reminder

import android.content.Context
import com.tteumsae.app.location.LocationAccessPolicy
import org.json.JSONObject

interface ActiveTripPreferences {
    fun read(): String?
    fun write(value: String)
    fun clear()
}

class ActiveTripStore(
    private val preferences: ActiveTripPreferences,
    private val locationMode: String = LocationAccessPolicy.persistenceMode,
) {
    fun save(trip: ActiveTrip) {
        if (!isCompatible(trip)) {
            clear()
            return
        }
        preferences.write(ActiveTripCodec.encode(trip))
    }

    fun loadValid(nowEpochMillis: Long = System.currentTimeMillis()): ActiveTrip? {
        val trip = preferences.read()
            ?.let { runCatching { ActiveTripCodec.decode(it) }.getOrNull() }
        if (trip == null || !isCompatible(trip) || trip.expiresAtEpochMillis <= nowEpochMillis) {
            preferences.clear()
            return null
        }
        return trip
    }

    fun clear() = preferences.clear()

    internal fun isCompatible(trip: ActiveTrip): Boolean =
        trip.locationMode == locationMode && trip.navigationToken.isNotBlank()
}

class SharedPreferencesActiveTripPreferences(context: Context) : ActiveTripPreferences {
    private val applicationContext = context.applicationContext
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun read(): String? = preferences.getString(KEY_ACTIVE_TRIP, null)

    override fun write(value: String) {
        cancelPreviousNavigation()
        preferences.edit().putString(KEY_ACTIVE_TRIP, value).apply()
    }

    override fun clear() {
        cancelPreviousNavigation()
        preferences.edit().remove(KEY_ACTIVE_TRIP).apply()
    }

    private fun cancelPreviousNavigation() {
        // The old app put its coordinates directly in an external PendingIntent.
        // Revoke that exact token before removing the only copy of its URI.
        val previousUrl = read()?.let { value ->
            runCatching { JSONObject(value).optString("navigationUrl").takeIf(String::isNotBlank) }.getOrNull()
        }
        ReminderNotifications.cancel(applicationContext, previousUrl)
    }

    private companion object {
        const val PREFERENCES_NAME = "active_trip"
        const val KEY_ACTIVE_TRIP = "snapshot"
    }
}
