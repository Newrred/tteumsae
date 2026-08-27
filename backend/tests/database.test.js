import test from "node:test";
import assert from "node:assert/strict";
import { listPlaces } from "../lib/database.js";

test("새 Supabase secret 키는 PostgREST Bearer 토큰으로 보내지 않는다", async () => {
  process.env.SUPABASE_URL = "https://supabase.test";
  process.env.SUPABASE_SERVICE_ROLE_KEY = "sb_secret_server-only-key";
  const originalFetch = globalThis.fetch;
  let request;
  globalThis.fetch = async (url, init) => {
    request = { url: String(url), init };
    return Response.json([]);
  };
  try {
    assert.deepEqual(await listPlaces(), []);
    assert.equal(request.init.headers.apikey, "sb_secret_server-only-key");
    assert.equal("authorization" in request.init.headers, false);
  } finally {
    globalThis.fetch = originalFetch;
  }
});

test("정규화된 TourAPI 상세 컬럼을 공개하고 raw는 숨긴다", async () => {
  process.env.SUPABASE_URL = "https://supabase.test";
  process.env.SUPABASE_SERVICE_ROLE_KEY = "service-role";
  const originalFetch = globalThis.fetch;
  globalThis.fetch = async () =>
    Response.json([
      {
        content_id: "123",
        source: "TOUR_API",
        name: "테스트 카페",
        category: "CAFE",
        content_type_id: 39,
        area_code: 32,
        latitude: 37.75,
        longitude: 128.87,
        default_stay_minutes: 40,
        cat1: "A05",
        cat2: "A0502",
        cat3: "A05020900",
        opening_hours: "09:00~18:00",
        closed_days: "매주 월요일",
        image_urls: ["https://example.com/1.jpg"],
        tags: ["주차 가능"],
        raw: { _tteumsae: { openingHours: "00:00~01:00" } }
      }
    ]);
  try {
    const [place] = await listPlaces({ limit: 1 });
    assert.equal(place.opening_hours, "09:00~18:00");
    assert.equal(place.cat3, "A05020900");
    assert.equal("raw" in place, false);
  } finally {
    globalThis.fetch = originalFetch;
  }
});
