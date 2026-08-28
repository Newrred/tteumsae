import test from "node:test";
import assert from "node:assert/strict";
import {
  fetchKakaoRoute,
  fetchKakaoRoutes,
  parseKakaoRoute
} from "../lib/kakao-mobility.js";
import { UpstreamTimeoutError } from "../lib/fetch-policy.js";

const start = { latitude: 37.7519, longitude: 128.8761 };
const destination = { latitude: 37.7644, longitude: 128.8996 };
const place = {
  content_id: "100",
  latitude: 37.758,
  longitude: 128.887
};

const successPayload = {
  routes: [
    {
      result_code: 0,
      summary: { distance: 9400, duration: 1260, fare: { toll: 1800 } },
      sections: [
        { distance: 4200, duration: 601 },
        { distance: 5200, duration: 659 }
      ]
    }
  ]
};

test("카카오 응답을 구간별 분 단위 경로로 변환한다", () => {
  const route = parseKakaoRoute(successPayload, start, destination, place);

  assert.equal(route.firstLegMinutes, 11);
  assert.equal(route.secondLegMinutes, 11);
  assert.equal(route.totalDistanceMeters, 9400);
  assert.equal(route.durationMinutes, 21);
  assert.equal(route.tollFare, 1800);
  assert.equal(route.provider, "KAKAO_MOBILITY");
});

test("직행과 경유지 5개 응답을 같은 형식으로 변환한다", () => {
  const direct = parseKakaoRoute(
    {
      routes: [{
        result_code: 0,
        summary: { distance: 9000, duration: 1200, fare: { toll: 0 } },
        sections: [{ distance: 9000, duration: 1200 }]
      }]
    },
    start,
    destination,
    []
  );
  assert.equal(direct.waypointCount, 0);
  assert.equal(direct.durationMinutes, 20);
  assert.equal(direct.legs.length, 1);

  const waypoints = Array.from({ length: 5 }, (_, index) => ({
    latitude: 37.755 + index * 0.001,
    longitude: 128.88 + index * 0.001
  }));
  const withFive = parseKakaoRoute(
    {
      routes: [{
        result_code: 0,
        summary: { distance: 12_000, duration: 1800 },
        sections: Array.from({ length: 6 }, () => ({
          distance: 2000,
          duration: 300
        }))
      }]
    },
    start,
    destination,
    waypoints
  );
  assert.equal(withFive.waypointCount, 5);
  assert.equal(withFive.legs.length, 6);
  assert.equal(withFive.tollFare, 0);
});

test("자동차 길찾기 요청에 경유지와 REST API 키를 적용한다", async () => {
  let request;
  const route = await fetchKakaoRoute(start, destination, place, {
    apiKey: "test-key",
    fetchImpl: async (url, options) => {
      request = { url, options };
      return {
        ok: true,
        async json() {
          return successPayload;
        }
      };
    }
  });

  const url = new URL(request.url);
  assert.equal(url.searchParams.get("waypoints"), "128.887,37.758");
  assert.equal(url.searchParams.get("priority"), "TIME");
  assert.equal(request.options.headers.authorization, "KakaoAK test-key");
  assert.equal(route.provider, "KAKAO_MOBILITY");
});

test("직행 요청은 waypoints 파라미터를 보내지 않고 여러 경유지는 순서대로 연결한다", async () => {
  const urls = [];
  const fetchImpl = async (url) => {
    urls.push(new URL(url));
    return {
      ok: true,
      async json() {
        const count = new URL(url).searchParams.get("waypoints")?.split("|").length ?? 0;
        return {
          routes: [{
            result_code: 0,
            summary: { distance: 1000, duration: 600 },
            sections: Array.from({ length: count + 1 }, () => ({
              distance: 1000,
              duration: 600
            }))
          }]
        };
      }
    };
  };
  await fetchKakaoRoute(start, destination, [], { apiKey: "test-key", fetchImpl });
  await fetchKakaoRoute(start, destination, [place, { ...place, longitude: 128.888 }], {
    apiKey: "test-key",
    fetchImpl
  });

  assert.equal(urls[0].searchParams.has("waypoints"), false);
  assert.equal(
    urls[1].searchParams.get("waypoints"),
    "128.887,37.758|128.888,37.758"
  );
});

test("후보별 실패는 제외하고 성공한 경로만 반환한다", async () => {
  const places = [
    place,
    { ...place, content_id: "200", longitude: 128.888 }
  ];
  const result = await fetchKakaoRoutes(start, destination, places, {
    concurrency: 2,
    apiKey: "test-key",
    fetchImpl: async (url) => {
      if (new URL(url).searchParams.get("waypoints").startsWith("128.888")) {
        return { ok: false, status: 403 };
      }
      return {
        ok: true,
        async json() {
          return successPayload;
        }
      };
    }
  });

  assert.equal(result.routes.size, 1);
  assert.equal(result.routes.get("100").provider, "KAKAO_MOBILITY");
  assert.equal(result.failedCount, 1);
});

test("Kakao Mobility 요청은 timeout signal을 전달한다", async () => {
  const route = await fetchKakaoRoute(start, destination, place, {
    apiKey: "test-key",
    fetchImpl: async (_url, options) => {
      assert.ok(options.signal instanceof AbortSignal);
      return Response.json(successPayload);
    }
  });
  assert.equal(route.provider, "KAKAO_MOBILITY");
});

test("후보 경로가 모두 timeout이면 정규화 오류를 보존한다", async () => {
  const timeout = new UpstreamTimeoutError("KAKAO_MOBILITY");
  await assert.rejects(
    fetchKakaoRoutes(start, destination, [place], {
      apiKey: "test-key",
      fetchImpl: async () => { throw timeout; }
    }),
    (error) => error === timeout
  );
});
