package com.tteumsae.app.reminder

import com.tteumsae.app.domain.Coordinates
import org.json.JSONObject

private const val ACTIVE_TRIP_RETENTION_MILLIS = 2 * 60 * 60_000L

data class ActiveTrip(
    val startName: String,
    val start: Coordinates,
    val destinationName: String,
    val destination: Coordinates,
    val stopId: String,
    val stopName: String,
    val stop: Coordinates,
    val arrivalDeadlineEpochMillis: Long,
    val latestDepartureEpochMillis: Long,
    val navigationUrl: String,
    val expiresAtEpochMillis: Long,
)

internal fun activeTripExpiryEpochMillis(arrivalDeadlineEpochMillis: Long): Long =
    arrivalDeadlineEpochMillis + ACTIVE_TRIP_RETENTION_MILLIS

internal object ActiveTripCodec {
    fun encode(trip: ActiveTrip): String = JSONObject()
        .put("startName", trip.startName)
        .put("start", trip.start.toJson())
        .put("destinationName", trip.destinationName)
        .put("destination", trip.destination.toJson())
        .put("stopId", trip.stopId)
        .put("stopName", trip.stopName)
        .put("stop", trip.stop.toJson())
        .put("arrivalDeadlineEpochMillis", trip.arrivalDeadlineEpochMillis)
        .put("latestDepartureEpochMillis", trip.latestDepartureEpochMillis)
        .put("navigationUrl", trip.navigationUrl)
        .put("expiresAtEpochMillis", trip.expiresAtEpochMillis)
        .toString()

    fun decode(value: String): ActiveTrip = JSONObject(value).let { json ->
        ActiveTrip(
            startName = json.getString("startName"),
            start = json.getJSONObject("start").toCoordinates(),
            destinationName = json.getString("destinationName"),
            destination = json.getJSONObject("destination").toCoordinates(),
            stopId = json.getString("stopId"),
            stopName = json.getString("stopName"),
            stop = json.getJSONObject("stop").toCoordinates(),
            arrivalDeadlineEpochMillis = json.getLong("arrivalDeadlineEpochMillis"),
            latestDepartureEpochMillis = json.getLong("latestDepartureEpochMillis"),
            navigationUrl = json.getString("navigationUrl"),
            expiresAtEpochMillis = json.getLong("expiresAtEpochMillis"),
        )
    }

    private fun Coordinates.toJson(): JSONObject = JSONObject()
        .put("latitude", latitude)
        .put("longitude", longitude)

    private fun JSONObject.toCoordinates(): Coordinates = Coordinates(
        latitude = getDouble("latitude"),
        longitude = getDouble("longitude"),
    )
}
