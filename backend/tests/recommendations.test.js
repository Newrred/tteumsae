import test from "node:test";
import assert from "node:assert/strict";
import recommendationsApi from "../api/recommendations.js";

const start = { latitude: 37.75, longitude: 128.87 };
const destination = { latitude: 37.75, longitude: 128.9 };

test("순수 여유시간으로 자동차 추천의 유효 마감과 corridor를 계산한다", async () => {
  const originalFetch = globalThis.fetch;
  process.env.KAKAO_REST_API_KEY = "test-key";
  process.env.SUPABASE_URL = "https://supabase.test";
  process.env.SUPABASE_SERVICE_ROLE_KEY = "service-key";
  globalThis.fetch = async (url) => {
    const parsed = new URL(url);
    if (parsed.hostname === "supabase.test") {
      return {
        ok: true,
        status: 200,
        async text() {
          return JSON.stringify([
            {
              content_id: "100",
              source: "TOUR_API",
              name: "경로 옆 장소",
              category: "ATTRACTION",
              latitude: 37.7505,
              longitude: 128.885,
              default_stay_minutes: 20,
              raw: {}
            },
            {
              content_id: "200",
              source: "TOUR_API",
              name: "바운드 모서리 장소",
              category: "ATTRACTION",
              latitude: 37.762,
              longitude: 128.915,
              default_stay_minutes: 20,
              raw: {}
            }
          ]);
        }
      };
    }

    const waypointCount = parsed.searchParams.get("waypoints")?.split("|").length ?? 0;
    return {
      ok: true,
      async json() {
        return {
          routes: [{
            result_code: 0,
            summary: {
              distance: waypointCount ? 3500 : 3000,
              duration: waypointCount ? 720 : 600,
              fare: { toll: 0 }
            },
            sections: Array.from({ length: waypointCount + 1 }, () => ({
              distance: waypointCount ? 1750 : 3000,
              duration: waypointCount ? 360 : 600,
              roads: [{ vertexes: [128.87, 37.75, 128.9, 37.75] }]
            }))
          }]
        };
      }
    };
  };

  try {
    const response = await recommendationsApi.fetch(new Request(
      "https://example.test/api/recommendations",
      {
        method: "POST",
        headers: { "content-type": "application/json" },
        body: JSON.stringify({
          mode: "ON_THE_WAY",
          start,
          destination,
          extraTimeMinutes: 45,
          safetyBufferMinutes: 15,
          transport: "CAR",
          categories: []
        })
      }
    ));
    const body = await response.json();
    assert.equal(response.status, 200);
    assert.equal(body.meta.baseRoute.durationMinutes, 10);
    assert.equal(body.baseRoute.totalDrivingMinutes, 10);
    assert.equal(body.meta.baseRouteMinutes, 10);
    assert.equal(body.meta.extraTimeMinutes, 45);
    assert.equal(body.meta.effectiveDeadlineMinutes, 55);
    assert.equal(body.meta.safetyBufferMinutes, 15);
    assert.equal(body.meta.corridorRadiusMeters, 900);
    assert.equal(body.meta.candidateCount, 2);
    assert.equal(body.meta.corridorCandidateCount, 1);
    assert.equal(body.data.length, 1);
    assert.equal(body.data[0].route.directMinutes, 10);
  } finally {
    globalThis.fetch = originalFetch;
  }
});

test("고비용 추천 요청을 IP별 분당 12회로 제한한다", async () => {
  const headers = { "x-forwarded-for": "203.0.113.42" };
  for (let count = 0; count < 12; count += 1) {
    const response = await recommendationsApi.fetch(new Request(
      "https://example.test/api/recommendations",
      { method: "POST", headers }
    ));
    assert.equal(response.status, 400);
  }

  const limited = await recommendationsApi.fetch(new Request(
    "https://example.test/api/recommendations",
    { method: "POST", headers }
  ));
  assert.equal(limited.status, 429);
  assert.equal((await limited.json()).error.code, "RATE_LIMITED");
  assert.ok(Number(limited.headers.get("retry-after")) >= 1);
});

test("근처 자동차 탐색은 출발지와 같은 목적지의 기본 경로를 요청하지 않는다", async () => {
  const originalFetch = globalThis.fetch;
  process.env.KAKAO_REST_API_KEY = "test-key";
  process.env.SUPABASE_URL = "https://supabase.test";
  process.env.SUPABASE_SERVICE_ROLE_KEY = "service-key";
  let directRouteRequestCount = 0;
  globalThis.fetch = async (url) => {
    const parsed = new URL(url);
    if (parsed.hostname === "supabase.test") {
      return {
        ok: true,
        status: 200,
        async text() {
          return JSON.stringify([{
            content_id: "100",
            source: "TOUR_API",
            name: "근처 장소",
            category: "ATTRACTION",
            latitude: 37.751,
            longitude: 128.871,
            default_stay_minutes: 20,
            raw: {}
          }]);
        }
      };
    }

    const waypointCount = parsed.searchParams.get("waypoints")?.split("|").length ?? 0;
    if (waypointCount === 0) directRouteRequestCount += 1;
    return {
      ok: true,
      async json() {
        return {
          routes: [{
            result_code: 0,
            summary: { distance: 2000, duration: 600, fare: { toll: 0 } },
            sections: Array.from({ length: waypointCount + 1 }, () => ({
              distance: 1000,
              duration: 300
            }))
          }]
        };
      }
    };
  };

  try {
    const response = await recommendationsApi.fetch(new Request(
      "https://example.test/api/recommendations",
      {
        method: "POST",
        headers: { "content-type": "application/json" },
        body: JSON.stringify({
          mode: "NEARBY",
          start,
          destination: start,
          deadlineMinutes: 90,
          safetyBufferMinutes: 15,
          transport: "CAR",
          categories: []
        })
      }
    ));
    const body = await response.json();
    assert.equal(response.status, 200);
    assert.equal(directRouteRequestCount, 0);
    assert.equal(body.baseRoute, undefined);
    assert.equal(body.data.length, 1);
  } finally {
    globalThis.fetch = originalFetch;
  }
});
