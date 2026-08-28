package com.tteumsae.app.ui.route

import androidx.lifecycle.SavedStateHandle
import com.tteumsae.app.data.RecommendationResult
import com.tteumsae.app.data.route.RouteGateway
import com.tteumsae.app.data.route.RouteWaypoint
import com.tteumsae.app.domain.Coordinates
import com.tteumsae.app.domain.OperationStatus
import com.tteumsae.app.domain.PlaceCandidate
import com.tteumsae.app.domain.PlaceCategory
import com.tteumsae.app.domain.RouteSummary
import com.tteumsae.app.domain.SafeRecommendation
import com.tteumsae.app.domain.SafetyLevel
import com.tteumsae.app.domain.SearchCriteria
import com.tteumsae.app.domain.route.RouteLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RouteFlowViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val now = 1_787_899_800_000L

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `유효한 검색은 로딩 뒤 결과로 전환된다`() = runTest(dispatcher) {
        val gateway = FakeRouteGateway(results = ArrayDeque(listOf(result(recommendation("one")))))
        val viewModel = viewModel(gateway)
        validInput(viewModel)

        viewModel.search()
        assertEquals(RouteStage.LOADING, viewModel.uiState.value.stage)
        advanceUntilIdle()

        assertEquals(RouteStage.RESULTS, viewModel.uiState.value.stage)
        assertEquals(listOf("one"), viewModel.uiState.value.recommendations.map { it.place.id })
        assertEquals(now, viewModel.uiState.value.calculatedAtEpochMillis)
    }

    @Test
    fun `빈 결과와 초기 검색 실패를 구분한다`() = runTest(dispatcher) {
        val emptyViewModel = viewModel(FakeRouteGateway(ArrayDeque(listOf(result()))))
        validInput(emptyViewModel)
        emptyViewModel.search()
        advanceUntilIdle()
        assertEquals(RouteStage.RESULTS, emptyViewModel.uiState.value.stage)
        assertTrue(emptyViewModel.uiState.value.recommendations.isEmpty())
        assertNull(emptyViewModel.uiState.value.errorMessage)

        val failedViewModel = viewModel(
            FakeRouteGateway(ArrayDeque(listOf(Result.failure(IllegalStateException("network"))))),
        )
        validInput(failedViewModel)
        failedViewModel.search()
        advanceUntilIdle()
        assertEquals(RouteStage.LOCATION, failedViewModel.uiState.value.stage)
        assertTrue(failedViewModel.uiState.value.errorMessage!!.isNotBlank())
    }

    @Test
    fun `한 곳 선택은 이전 선택을 교체하고 명시적으로 해제된다`() = runTest(dispatcher) {
        val viewModel = viewModel(
            FakeRouteGateway(
                ArrayDeque(listOf(result(recommendation("one"), recommendation("two")))),
            ),
        )
        validInput(viewModel)
        viewModel.search()
        advanceUntilIdle()

        viewModel.selectPlace("one")
        viewModel.selectPlace("two")
        assertEquals("two", viewModel.uiState.value.selectedPlaceId)
        viewModel.clearSelection()
        assertNull(viewModel.uiState.value.selectedPlaceId)
    }

    @Test
    fun `새 검색은 이전 선택을 먼저 비운다`() = runTest(dispatcher) {
        val gateway = FakeRouteGateway(
            ArrayDeque(
                listOf(
                    result(recommendation("one")),
                    result(recommendation("two")),
                ),
            ),
        )
        val viewModel = viewModel(gateway)
        validInput(viewModel)
        viewModel.search()
        advanceUntilIdle()
        viewModel.selectPlace("one")

        viewModel.search()
        assertNull(viewModel.uiState.value.selectedPlaceId)
        advanceUntilIdle()
        assertEquals(listOf("two"), viewModel.uiState.value.recommendations.map { it.place.id })
    }

    @Test
    fun `새로고침 실패는 마지막 안전 결과를 보존한다`() = runTest(dispatcher) {
        val gateway = FakeRouteGateway(
            ArrayDeque(
                listOf(
                    result(recommendation("one")),
                    Result.failure(IllegalStateException("refresh failed")),
                ),
            ),
        )
        val viewModel = viewModel(gateway)
        validInput(viewModel)
        viewModel.search()
        advanceUntilIdle()

        viewModel.refresh()
        assertTrue(viewModel.uiState.value.isRefreshing)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isRefreshing)
        assertEquals(listOf("one"), viewModel.uiState.value.recommendations.map { it.place.id })
        assertTrue(viewModel.uiState.value.warning.isNotBlank())
    }

    @Test
    fun `복원 데이터에 추천 payload가 없으면 위치 단계에서 재개한다`() = runTest(dispatcher) {
        val savedStateHandle = SavedStateHandle()
        val first = viewModel(FakeRouteGateway(ArrayDeque(listOf(result(recommendation("one"))))), savedStateHandle)
        validInput(first)
        first.updateFilters(setOf(PlaceCategory.CAFE))
        first.search()
        advanceUntilIdle()
        first.selectPlace("one")

        val restored = viewModel(FakeRouteGateway(), savedStateHandle)

        assertEquals(RouteStage.LOCATION, restored.uiState.value.stage)
        assertTrue(restored.uiState.value.recommendations.isEmpty())
        assertEquals("강릉역", restored.uiState.value.input.start?.name)
        assertEquals("경포대", restored.uiState.value.input.destination?.name)
        assertEquals(now + 60 * 60_000, restored.uiState.value.input.arrivalDeadlineEpochMillis)
        assertEquals(setOf(PlaceCategory.CAFE), restored.uiState.value.input.categories)
        assertEquals("one", restored.uiState.value.selectedPlaceId)
    }

    private fun viewModel(
        gateway: RouteGateway,
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
    ) = RouteFlowViewModel(savedStateHandle, gateway) { now }

    private fun validInput(viewModel: RouteFlowViewModel) {
        viewModel.updateStart(RouteLocation("강릉역", Coordinates(37.75, 128.87)))
        viewModel.updateDestination(RouteLocation("경포대", Coordinates(37.80, 128.90)))
        viewModel.updateDeadline(now + 60 * 60_000)
    }

    private fun result(vararg recommendations: SafeRecommendation) = Result.success(
        RecommendationResult(
            recommendations = recommendations.toList(),
            warning = "",
            calculatedAtEpochMillis = now,
            arrivalDeadlineEpochMillis = now + 60 * 60_000,
            minimumStayMinutes = 15,
        ),
    )

    private fun recommendation(id: String) = SafeRecommendation(
        place = PlaceCandidate(
            id = id,
            name = id,
            category = PlaceCategory.CAFE,
            stayMinutes = 0,
            firstLegMinutes = 10,
            secondLegMinutes = 10,
            detourMinutes = 5,
            reason = "",
            tags = emptyList(),
            latitude = 37.76,
            longitude = 128.88,
        ),
        totalMinutes = 20,
        marginMinutes = 35,
        safetyLevel = SafetyLevel.AVAILABLE,
        operationStatus = OperationStatus.UNKNOWN,
        minimumStayMinutes = 15,
        maximumStayMinutes = 35,
        latestDepartureEpochMillis = now + 40 * 60_000,
    )

    private class FakeRouteGateway(
        private val results: ArrayDeque<Result<RecommendationResult>> = ArrayDeque(),
    ) : RouteGateway {
        override suspend fun recommendations(criteria: SearchCriteria): RecommendationResult =
            results.removeFirst().getOrThrow()

        override suspend fun calculateRoute(
            start: Coordinates,
            destination: Coordinates,
            waypoints: List<RouteWaypoint>,
        ): RouteSummary = error("not used")
    }
}
