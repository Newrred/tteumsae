package com.tteumsae.app.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in SUPPORTED_ACTIONS) return
        val now = System.currentTimeMillis()
        val store = ActiveTripStore(SharedPreferencesActiveTripPreferences(context.applicationContext))
        val trip = store.loadValid(now) ?: return
        if (
            shouldRescheduleTrip(
                latestDepartureEpochMillis = trip.latestDepartureEpochMillis,
                expiresAtEpochMillis = trip.expiresAtEpochMillis,
                nowEpochMillis = now,
            )
        ) {
            AlarmManagerDepartureReminderScheduler(context.applicationContext).schedule(trip, now)
        }
    }

    private companion object {
        val SUPPORTED_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
        )
    }
}
