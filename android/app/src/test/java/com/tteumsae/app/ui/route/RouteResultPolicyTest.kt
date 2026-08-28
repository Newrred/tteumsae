package com.tteumsae.app.ui.route

import com.tteumsae.app.domain.PlaceCandidate
import com.tteumsae.app.domain.PlaceCategory
import com.tteumsae.app.domain.SafeRecommendation
import com.tteumsae.app.domain.SafetyLevel
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RouteResultPolicyTest {
    @Test
    fun `지도 핀은 추가 이동시간만 표시한다`() {
        assertEquals("+8분", detourPinLabel(recommendation("one", detour = 8)))
    }

    @Test
    fun `장소 선택은 항상 한 곳이며 같은 장소를 누르면 해제된다`() {
        assertEquals("one", nextSelectedPlaceId(null, "one"))
        assertEquals("two", nextSelectedPlaceId("one", "two"))
        assertNull(nextSelectedPlaceId("one", "one"))
    }

    @Test
    fun `선택 요약은 최대 체류와 출발 권장시각을 사용한다`() {
        val recommendation = recommendation("one", detour = 8)

        assertEquals(
            "이동 기준 최대 약 35분",
            maximumStayLabel(recommendation),
        )
        assertEquals(
            "오후 6시 20분까지 출발하면 돼요",
            latestDepartureLabel(
                recommendation,
                zoneId = ZoneId.of("Asia/Seoul"),
            ),
        )
    }

    @Test
    fun `제품 길 안내에는 선택한 경유지 한 곳만 전달한다`() {
        val recommendations = listOf(
            recommendation("one", 5),
            recommendation("two", 8),
        )

        assertEquals(
            listOf("two"),
            productNavigationRecommendations("two", recommendations).map { it.place.id },
        )
        assertEquals(emptyList<SafeRecommendation>(), productNavigationRecommendations(null, recommendations))
    }

    private fun recommendation(id: String, detour: Int) = SafeRecommendation(
        place = PlaceCandidate(
            id = id,
            name = id,
            category = PlaceCategory.CAFE,
            stayMinutes = 0,
            firstLegMinutes = 10,
            secondLegMinutes = 10,
            detourMinutes = detour,
            reason = "",
            tags = emptyList(),
        ),
        totalMinutes = 20,
        marginMinutes = 35,
        safetyLevel = SafetyLevel.AVAILABLE,
        minimumStayMinutes = 15,
        maximumStayMinutes = 35,
        latestDepartureEpochMillis = 1_787_908_800_000,
    )
}
