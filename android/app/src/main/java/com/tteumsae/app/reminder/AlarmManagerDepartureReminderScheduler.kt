package com.tteumsae.app.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

class AlarmManagerDepartureReminderScheduler(
    private val context: Context,
) : DepartureReminderScheduler {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(trip: ActiveTrip, nowEpochMillis: Long) {
        when (
            val decision = departureReminderDecision(
                latestDepartureEpochMillis = trip.latestDepartureEpochMillis,
                nowEpochMillis = nowEpochMillis,
            )
        ) {
            is DepartureReminderDecision.Schedule -> alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                decision.triggerAtEpochMillis,
                reminderPendingIntent(),
            )
            DepartureReminderDecision.NotifyNow -> ReminderNotifications.show(context, trip)
            DepartureReminderDecision.Skip -> cancel()
        }
    }

    override fun cancel() {
        alarmManager.cancel(reminderPendingIntent())
    }

    private fun reminderPendingIntent(): PendingIntent = PendingIntent.getBroadcast(
        context,
        REQUEST_CODE,
        Intent(context, DepartureReminderReceiver::class.java).setAction(ACTION_DEPARTURE_REMINDER),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    companion object {
        const val ACTION_DEPARTURE_REMINDER = "com.tteumsae.app.action.DEPARTURE_REMINDER"
        private const val REQUEST_CODE = 4105
    }
}
