package com.tteumsae.app.ui.route

import com.tteumsae.app.domain.PlaceCandidate

internal fun normalizedVisitInfo(value: String?): String? = value
    ?.trim()
    ?.takeUnless { it.isBlank() || it.equals("null", ignoreCase = true) }

internal fun readableDuration(minutes: Int): String {
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    return buildString {
        if (hours > 0) append("${hours}시간")
        if (hours > 0 && remainingMinutes > 0) append(' ')
        if (remainingMinutes > 0 || hours == 0) append("${remainingMinutes}분")
    }
}

internal fun explicitActivityLabel(tags: List<String>): String? = tags.firstOrNull {
    it in setOf("야외 활동", "실내 활동", "실내·외 활동")
}

internal data class PlaceVisitFact(
    val label: String,
    val value: String,
)

internal fun mergeFreshPlaceDetails(
    routePlace: PlaceCandidate,
    freshPlace: PlaceCandidate,
): PlaceCandidate {
    fun preferFresh(fresh: String, original: String): String =
        normalizedVisitInfo(fresh) ?: original

    return routePlace.copy(
        name = preferFresh(freshPlace.name, routePlace.name),
        category = freshPlace.category,
        tags = freshPlace.tags.ifEmpty { routePlace.tags },
        address = preferFresh(freshPlace.address, routePlace.address),
        imageUrl = preferFresh(freshPlace.imageUrl, routePlace.imageUrl),
        latitude = freshPlace.latitude ?: routePlace.latitude,
        longitude = freshPlace.longitude ?: routePlace.longitude,
        openingHours = preferFresh(freshPlace.openingHours, routePlace.openingHours),
        closedDays = preferFresh(freshPlace.closedDays, routePlace.closedDays),
        telephone = preferFresh(freshPlace.telephone, routePlace.telephone),
        homepageUrl = preferFresh(freshPlace.homepageUrl, routePlace.homepageUrl),
        overview = preferFresh(freshPlace.overview, routePlace.overview),
        imageUrls = freshPlace.imageUrls.ifEmpty { routePlace.imageUrls },
        lastAdmission = preferFresh(freshPlace.lastAdmission, routePlace.lastAdmission),
        parkingInfo = preferFresh(freshPlace.parkingInfo, routePlace.parkingInfo),
        eventStartDate = preferFresh(freshPlace.eventStartDate, routePlace.eventStartDate),
        eventEndDate = preferFresh(freshPlace.eventEndDate, routePlace.eventEndDate),
        dataProvenance = preferFresh(freshPlace.dataProvenance, routePlace.dataProvenance),
        operatingInfoStatus = preferFresh(
            freshPlace.operatingInfoStatus,
            routePlace.operatingInfoStatus,
        ),
        admissionInfoStatus = preferFresh(
            freshPlace.admissionInfoStatus,
            routePlace.admissionInfoStatus,
        ),
        parkingInfoStatus = preferFresh(
            freshPlace.parkingInfoStatus,
            routePlace.parkingInfoStatus,
        ),
        reviewedAt = preferFresh(freshPlace.reviewedAt, routePlace.reviewedAt),
    )
}

internal fun practicalVisitFacts(place: PlaceCandidate): List<PlaceVisitFact> = buildList {
    normalizedVisitInfo(place.openingHours)?.let { add(PlaceVisitFact("운영시간", it)) }
    normalizedVisitInfo(place.closedDays)?.let {
        add(PlaceVisitFact("휴무일", structuredClosedDays(it)))
    }
    normalizedVisitInfo(place.lastAdmission)?.let { add(PlaceVisitFact("입장 마감", it)) }
    eventPeriodLabel(place.eventStartDate, place.eventEndDate)?.let {
        add(PlaceVisitFact("행사 기간", it))
    }
    normalizedVisitInfo(place.parkingInfo)?.let { add(PlaceVisitFact("주차", it)) }
}

internal fun structuredClosedDays(value: String): String = value
    .split(Regex("\\s*/\\s*"))
    .map(String::trim)
    .filter(String::isNotBlank)
    .joinToString(separator = "\n") { "• $it" }
    .ifBlank { value.trim() }

internal fun eventPeriodLabel(start: String?, end: String?): String? {
    val normalizedStart = normalizedDate(start)
    val normalizedEnd = normalizedDate(end)
    return when {
        normalizedStart == null && normalizedEnd == null -> null
        normalizedStart == normalizedEnd -> normalizedStart
        normalizedStart == null -> "~ $normalizedEnd"
        normalizedEnd == null -> "$normalizedStart ~"
        else -> "$normalizedStart ~ $normalizedEnd"
    }
}

private fun normalizedDate(value: String?): String? {
    val digits = normalizedVisitInfo(value)?.filter(Char::isDigit) ?: return null
    if (digits.length != 8) return normalizedVisitInfo(value)
    return "${digits.substring(0, 4)}.${digits.substring(4, 6)}.${digits.substring(6, 8)}"
}

internal fun plainTourText(value: String?): String? = normalizedVisitInfo(value)
    ?.replace(Regex("(?i)<br\\s*/?>"), "\n")
    ?.replace(Regex("<[^>]+>"), "")
    ?.replace("&nbsp;", " ")
    ?.replace("&amp;", "&")
    ?.replace("&lt;", "<")
    ?.replace("&gt;", ">")
    ?.replace("&quot;", "\"")
    ?.replace(Regex("[ \\t]+"), " ")
    ?.replace(Regex("\\n{3,}"), "\n\n")
    ?.trim()
    ?.takeIf(String::isNotBlank)

internal fun normalizedHomepageUrl(value: String?): String? {
    val raw = normalizedVisitInfo(value) ?: return null
    val href = Regex("(?i)href\\s*=\\s*[\"']([^\"']+)[\"']")
        .find(raw)
        ?.groupValues
        ?.getOrNull(1)
    return (href ?: plainTourText(raw))
        ?.trim()
        ?.takeIf { it.startsWith("https://") || it.startsWith("http://") }
}

internal fun placeSourceCaption(place: PlaceCandidate): String {
    val source = if (place.dataProvenance.equals("CURATION", ignoreCase = true)) {
        "공식 자료 확인 · 한국관광공사 TourAPI"
    } else {
        "한국관광공사 TourAPI"
    }
    val reviewed = normalizedVisitInfo(place.reviewedAt)
        ?.take(10)
        ?.replace('-', '.')
    return if (reviewed == null) source else "$source · $reviewed 확인"
}
