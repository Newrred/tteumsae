package com.tteumsae.app.ui

import com.tteumsae.app.domain.Coordinates
import com.tteumsae.app.domain.PlaceCategory
import com.tteumsae.app.domain.TransportMode
import org.junit.Assert.assertEquals
import org.junit.Test

class KakaoMapRouteTest {
    @Test
    fun `자동차 경로에 출발지 경유지 도착지를 포함한다`() {
        assertEquals(
            "sp=37.1,128.1&vp=37.2,128.2&ep=37.3,128.3&by=car",
            buildKakaoMapRouteQuery(
                start = Coordinates(37.1, 128.1),
                destination = Coordinates(37.3, 128.3),
                transport = TransportMode.CAR,
                waypoint = Coordinates(37.2, 128.2),
            ),
        )
    }

    @Test
    fun `근처 장소는 선택한 이동수단으로 바로 길 안내한다`() {
        assertEquals(
            "sp=37.1,128.1&ep=37.2,128.2&by=foot",
            buildKakaoMapRouteQuery(
                start = Coordinates(37.1, 128.1),
                destination = Coordinates(37.2, 128.2),
                transport = TransportMode.WALK,
            ),
        )
    }

    @Test
    fun `태그 폭 예산을 넘으면 남은 개수를 반환한다`() {
        assertEquals(
            listOf("주차 가능", "아이 동반", "점심 식사") to 1,
            compactTags(listOf("주차 가능", "아이 동반", "점심 식사", "반려동물 동반")),
        )
    }

    @Test
    fun `배가 부르면 음식점을 추천 대상에서 제외한다`() {
        assertEquals(
            PlaceCategory.entries.toSet() - PlaceCategory.RESTAURANT,
            recommendationCategories(emptySet(), excludeRestaurants = true),
        )
        assertEquals(
            setOf(PlaceCategory.CULTURE),
            recommendationCategories(setOf(PlaceCategory.CULTURE), excludeRestaurants = true),
        )
    }
}
