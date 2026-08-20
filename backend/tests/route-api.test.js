import test from "node:test";
import assert from "node:assert/strict";
import routeApi from "../api/route.js";

const start = { latitude: 37.7519, longitude: 128.8761 };
const destination = { latitude: 37.7644, longitude: 128.8996 };

test("경로 API가 카카오 요약과 기존 Android 호환 필드를 반환한다", async () => {
  const originalFetch = globalThis.fetch;
  process.env.KAKAO_REST_API_KEY = "test-key";
  globalThis.fetch = async () => ({
    ok: true,
    async json() {
      return {
        routes: [{
          result_code: 0,
          summary: { distance: 9400, duration: 1260, fare: { toll: 1800 } },
          sections: [{ distance: 9400, duration: 1260 }]
        }]
      };
    }
  });

  try {
    const response = await routeApi.fetch(new Request("https://example.test/api/route", {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify({ start, destination })
    }));
    const body = await response.json();
    assert.equal(response.status, 200);
    assert.equal(body.data.durationMinutes, 21);
    assert.equal(body.data.distanceMeters, 9400);
    assert.equal(body.data.tollFare, 1800);
    assert.equal(body.data.totalDrivingMinutes, 21);
    assert.equal(body.data.provider, "KAKAO_MOBILITY");
  } finally {
    globalThis.fetch = originalFetch;
  }
});

test("경로 API가 경유지 6개를 카카오 호출 전에 거부한다", async () => {
  const response = await routeApi.fetch(new Request("https://example.test/api/route", {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify({
      start,
      destination,
      waypoints: Array.from({ length: 6 }, () => start)
    })
  }));
  assert.equal(response.status, 400);
  assert.match((await response.json()).error.message, /최대 5개/);
});
