package com.tteumsae.app.ui.route

import com.tteumsae.app.domain.PlaceCandidate
import com.tteumsae.app.domain.PlaceCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DetailPresentationTest {
    @Test
    fun `closed day rules are separated into scannable lines`() {
        assertEquals(
            "• 매주 월요일\n• 1월 1일\n• 설·추석 당일",
            structuredClosedDays("매주 월요일 / 1월 1일 / 설·추석 당일"),
        )
    }

    @Test
    fun `방문 정보의 공백과 null 문자열은 없는 값으로 정규화한다`() {
        assertNull(normalizedVisitInfo(null))
        assertNull(normalizedVisitInfo(""))
        assertNull(normalizedVisitInfo("   "))
        assertNull(normalizedVisitInfo("null"))
        assertNull(normalizedVisitInfo(" NULL "))
        assertEquals("매일 09:00~18:00", normalizedVisitInfo(" 매일 09:00~18:00 "))
    }

    @Test
    fun `분 단위 시간은 읽기 쉬운 시간과 분으로 표시한다`() {
        assertEquals("45분", readableDuration(45))
        assertEquals("1시간", readableDuration(60))
        assertEquals("2시간 5분", readableDuration(125))
    }

    @Test
    fun `활동 정보는 명시된 태그만 사용한다`() {
        assertNull(explicitActivityLabel(emptyList()))
        assertNull(explicitActivityLabel(listOf("관광지", "가족 여행")))
        assertEquals("야외 활동", explicitActivityLabel(listOf("가족 여행", "야외 활동")))
    }

    @Test
    fun `fresh detail replaces metadata without losing route metrics`() {
        val routePlace = place().copy(
            firstLegMinutes = 12,
            secondLegMinutes = 18,
            detourMinutes = 7,
            overview = "검색 당시 소개",
        )
        val fresh = place().copy(
            firstLegMinutes = 0,
            secondLegMinutes = 0,
            detourMinutes = 0,
            overview = "최신 소개",
            telephone = "033-123-4567",
            parkingInfo = "건물 뒤 주차장",
        )

        val merged = mergeFreshPlaceDetails(routePlace, fresh)

        assertEquals(12, merged.firstLegMinutes)
        assertEquals(18, merged.secondLegMinutes)
        assertEquals(7, merged.detourMinutes)
        assertEquals("최신 소개", merged.overview)
        assertEquals("033-123-4567", merged.telephone)
        assertEquals("건물 뒤 주차장", merged.parkingInfo)
    }

    @Test
    fun `practical facts contain only supplied visit data`() {
        val facts = practicalVisitFacts(
            place().copy(
                openingHours = "09:00~18:00",
                closedDays = "null",
                parkingInfo = "무료 주차 20대",
                eventStartDate = "20260905",
                eventEndDate = "20260907",
            ),
        )

        assertEquals(listOf("운영시간", "행사 기간", "주차"), facts.map { it.label })
        assertEquals("2026.09.05 ~ 2026.09.07", facts[1].value)
    }

    @Test
    fun `TourAPI html is cleaned and homepage href is extracted`() {
        assertEquals(
            "첫 줄\n둘째 줄 & 안내",
            plainTourText("<p>첫 줄<br>둘째 줄 &amp; 안내</p>"),
        )
        assertEquals(
            "https://example.com/place",
            normalizedHomepageUrl("<a href='https://example.com/place'>홈페이지</a>"),
        )
        assertTrue(placeSourceCaption(place().copy(dataProvenance = "TOUR_API")).contains("TourAPI"))
    }

    private fun place() = PlaceCandidate(
        id = "tour:1",
        name = "장소",
        category = PlaceCategory.CULTURE,
        stayMinutes = 40,
        firstLegMinutes = 0,
        secondLegMinutes = 0,
        detourMinutes = 0,
        reason = "",
        tags = emptyList(),
    )
}
