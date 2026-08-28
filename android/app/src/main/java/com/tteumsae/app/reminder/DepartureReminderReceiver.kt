package com.tteumsae.app.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DepartureReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmManagerDepartureReminderScheduler.ACTION_DEPARTURE_REMINDER) return
        val store = ActiveTripStore(SharedPreferencesActiveTripPreferences(context.applicationContext))
        val trip = store.loadValid() ?: return
        if (System.currentTimeMillis() >= trip.latestDepartureEpochMillis) {
            store.clear()
            return
        }
        ReminderNotifications.show(context, trip)
    }
}
