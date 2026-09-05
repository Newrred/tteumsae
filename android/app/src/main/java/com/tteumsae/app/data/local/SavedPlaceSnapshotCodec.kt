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
        .put("telephone", place.telephone)
        .put("homepageUrl", place.homepageUrl)
        .put("overview", place.overview)
        .put("imageUrls", JSONArray(place.imageUrls))
        .put("lastAdmission", place.lastAdmission)
        .put("parkingInfo", place.parkingInfo)
        .put("eventStartDate", place.eventStartDate)
        .put("eventEndDate", place.eventEndDate)
        .put("dataProvenance", place.dataProvenance)
        .put("operatingInfoStatus", place.operatingInfoStatus)
        .put("admissionInfoStatus", place.admissionInfoStatus)
        .put("parkingInfoStatus", place.parkingInfoStatus)
        .put("reviewedAt", place.reviewedAt)
        .toString()

    fun decode(snapshotJson: String): PlaceCandidate? = runCatching {
        val json = JSONObject(snapshotJson)
        val tags = json.optJSONArray("tags")
        val imageUrls = json.optJSONArray("imageUrls")
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
            telephone = json.optString("telephone"),
            homepageUrl = json.optString("homepageUrl"),
            overview = json.optString("overview"),
            imageUrls = buildList {
                if (imageUrls != null) {
                    for (index in 0 until imageUrls.length()) {
                        imageUrls.optString(index).takeIf(String::isNotBlank)?.let(::add)
                    }
                }
            },
            lastAdmission = json.optString("lastAdmission"),
            parkingInfo = json.optString("parkingInfo"),
            eventStartDate = json.optString("eventStartDate"),
            eventEndDate = json.optString("eventEndDate"),
            dataProvenance = json.optString("dataProvenance"),
            operatingInfoStatus = json.optString("operatingInfoStatus"),
            admissionInfoStatus = json.optString("admissionInfoStatus"),
            parkingInfoStatus = json.optString("parkingInfoStatus"),
            reviewedAt = json.optString("reviewedAt"),
        )
    }.getOrNull()
}

private fun JSONObject.optNullableDouble(key: String): Double? =
    if (!has(key) || isNull(key)) null else getDouble(key)
