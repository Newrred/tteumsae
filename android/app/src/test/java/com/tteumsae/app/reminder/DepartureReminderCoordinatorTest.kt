package com.tteumsae.app.reminder

import com.tteumsae.app.domain.Coordinates
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DepartureReminderCoordinatorTest {
    @Test
    fun `새 검색은 저장된 여행과 예약을 함께 제거한다`() {
        val fixture = Fixture()
        fixture.coordinator.enable(trip("old", 700_000))

        fixture.coordinator.clear()

        assertNull(fixture.store.loadValid(nowEpochMillis = 0))
        assertEquals(1, fixture.scheduler.cancelCount)
    }

    @Test
    fun `재조회에서 선택 장소가 사라지면 이전 알림을 제거한다`() {
        val fixture = Fixture()
        fixture.coordinator.enable(trip("old", 700_000))

        assertNull(fixture.coordinator.reconcile(null))

        assertNull(fixture.store.loadValid(nowEpochMillis = 0))
        assertEquals(1, fixture.scheduler.cancelCount)
    }

    @Test
    fun `재조회에서 같은 장소가 남으면 새 출발 마감으로 교체한다`() {
        val fixture = Fixture()
        fixture.coordinator.enable(trip("same", 700_000))
        val updated = trip("same", 760_000)

        assertEquals("same", fixture.coordinator.reconcile(updated))

        assertEquals(updated, fixture.store.loadValid(nowEpochMillis = 0))
        assertEquals(updated, fixture.scheduler.scheduled.last())
    }

    @Test
    fun `권한 응답이 늦어 출발 마감이 지나면 저장하거나 예약하지 않는다`() {
        val fixture = Fixture()

        assertNull(fixture.coordinator.enable(trip("late", -1)))

        assertNull(fixture.store.loadValid(nowEpochMillis = 0))
        assertEquals(emptyList<ActiveTrip>(), fixture.scheduler.scheduled)
        assertEquals(1, fixture.scheduler.cancelCount)
    }

    @Test
    fun `재조회한 출발 마감이 이미 지났으면 기존 알림을 제거한다`() {
        val fixture = Fixture()
        fixture.coordinator.enable(trip("same", 700_000))

        assertNull(fixture.coordinator.reconcile(trip("same", -1)))

        assertNull(fixture.store.loadValid(nowEpochMillis = 0))
        assertEquals(1, fixture.scheduler.cancelCount)
    }

    @Test
    fun `앱 복원 시 출발 마감이 지난 여행을 활성 알림으로 표시하지 않는다`() {
        val fixture = Fixture()
        fixture.store.save(trip("late", -1))

        assertNull(fixture.coordinator.currentEnabledStopId())

        assertNull(fixture.store.loadValid(nowEpochMillis = 0))
        assertEquals(1, fixture.scheduler.cancelCount)
    }

    private class Fixture {
        private val preferences = FakePreferences()
        val store = ActiveTripStore(preferences)
        val scheduler = FakeScheduler()
        val coordinator = DepartureReminderCoordinator(
            store = store,
            scheduler = scheduler,
            nowEpochMillis = { 0 },
        )
    }

    private class FakePreferences : ActiveTripPreferences {
        var value: String? = null
        override fun read(): String? = value
        override fun write(value: String) { this.value = value }
        override fun clear() { value = null }
    }

    private class FakeScheduler : DepartureReminderScheduler {
        val scheduled = mutableListOf<ActiveTrip>()
        var cancelCount = 0
        override fun schedule(trip: ActiveTrip, nowEpochMillis: Long) {
            scheduled += trip
        }
        override fun cancel() { cancelCount += 1 }
    }

    private fun trip(stopId: String, latestDeparture: Long) = ActiveTrip(
        startName = "출발",
        start = Coordinates(37.1, 128.1),
        destinationName = "도착",
        destination = Coordinates(37.3, 128.3),
        stopId = stopId,
        stopName = stopId,
        stop = Coordinates(37.2, 128.2),
        arrivalDeadlineEpochMillis = 1_000_000,
        latestDepartureEpochMillis = latestDeparture,
        navigationUrl = "https://map.kakao.com/$stopId",
        expiresAtEpochMillis = activeTripExpiryEpochMillis(1_000_000),
    )
}
