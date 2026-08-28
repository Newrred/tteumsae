package com.tteumsae.app.reminder

class DepartureReminderCoordinator(
    private val store: ActiveTripStore,
    private val scheduler: DepartureReminderScheduler,
    private val nowEpochMillis: () -> Long = System::currentTimeMillis,
) {
    fun enable(trip: ActiveTrip): String? {
        val now = nowEpochMillis()
        if (trip.latestDepartureEpochMillis <= now || trip.expiresAtEpochMillis <= now) {
            clear()
            return null
        }
        store.save(trip)
        scheduler.schedule(trip, now)
        return trip.stopId
    }

    fun clear() {
        scheduler.cancel()
        store.clear()
    }

    fun reconcile(updatedTrip: ActiveTrip?): String? {
        val currentStopId = currentEnabledStopId() ?: return null
        if (updatedTrip == null || updatedTrip.stopId != currentStopId) {
            clear()
            return null
        }
        return enable(updatedTrip)
    }

    fun currentEnabledStopId(): String? {
        val now = nowEpochMillis()
        val current = store.loadValid(now)
        if (current == null || current.latestDepartureEpochMillis <= now) {
            clear()
            return null
        }
        return current.stopId
    }
}
