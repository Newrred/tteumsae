package com.tteumsae.app.data.local

import com.tteumsae.app.domain.PlaceCandidate
import com.tteumsae.app.domain.PlaceCategory
import org.json.JSONArray
import org.json.JSONObject

object SavedPlaceSnapshotCodec {
    fun encode(place: PlaceCandidate): String = JSONObject()
        .put("id", place.id)
        .put("name", place.name)
        .put("category", place.category.name)
        .put("stayMinutes", place.stayMinutes)
        .put("tags", JSONArray(place.tags))
        .put("address", place.address)
        .put("imageUrl", place.imageUrl)
        .put("latitude", place.latitude ?: JSONObject.NULL)
        .put("longitude", place.longitude ?: JSONObject.NULL)
        .put("openingHours", place.openingHours)
        .put("closedDays", place.closedDays)
        .toString()

    fun decode(snapshotJson: String): PlaceCandidate? = runCatching {
        val json = JSONObject(snapshotJson)
        val tags = json.optJSONArray("tags")
        PlaceCandidate(
            id = json.getString("id"),
            name = json.getString("name"),
            category = PlaceCategory.valueOf(json.getString("category")),
            stayMinutes = json.getInt("stayMinutes"),
            firstLegMinutes = 0,
            secondLegMinutes = 0,
            detourMinutes = 0,
            firstLegDistanceMeters = 0,
            secondLegDistanceMeters = 0,
            reason = "",
            tags = buildList {
                if (tags != null) {
                    for (index in 0 until tags.length()) {
                        add(tags.getString(index))
                    }
                }
            },
            address = json.optString("address"),
            imageUrl = json.optString("imageUrl"),
            latitude = json.optNullableDouble("latitude"),
            longitude = json.optNullableDouble("longitude"),
            isOpen = true,
            openingHours = json.optString("openingHours"),
            closedDays = json.optString("closedDays"),
        )
    }.getOrNull()
}

private fun JSONObject.optNullableDouble(key: String): Double? =
    if (!has(key) || isNull(key)) null else getDouble(key)
