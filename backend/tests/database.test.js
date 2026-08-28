import test from "node:test";
import assert from "node:assert/strict";
import { claimSyncJob, finishSyncJob, listPlaces } from "../lib/database.js";

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

test("상세 컬럼 마이그레이션 전에는 레거시 장소 조회로 폴백한다", async () => {
  process.env.SUPABASE_URL = "https://supabase.test";
  process.env.SUPABASE_SERVICE_ROLE_KEY = "service-role";
  const originalFetch = globalThis.fetch;
  const requests = [];
  globalThis.fetch = async (url) => {
    requests.push(String(url));
    if (requests.length === 1) {
      return Response.json(
        { code: "42703", message: "column places.cat1 does not exist" },
        { status: 400 }
      );
    }
    return Response.json([
      {
        content_id: "legacy-123",
        source: "TOUR_API",
        name: "기존 장소",
        category: "RESTAURANT",
        content_type_id: 39,
        area_code: 32,
        latitude: 37.75,
        longitude: 128.87,
        default_stay_minutes: 40,
        raw: {
          _tteumsae: {
            openingHours: "10:00~20:00",
            imageUrls: ["https://example.com/legacy.jpg"],
            tags: ["주차 가능"]
          }
        }
      }
    ]);
  };
  try {
    const [place] = await listPlaces({ limit: 1 });
    assert.equal(requests.length, 2);
    assert.match(requests[0], /cat1/);
    assert.match(requests[1], /raw/);
    assert.equal(place.opening_hours, "10:00~20:00");
    assert.deepEqual(place.image_urls, ["https://example.com/legacy.jpg"]);
    assert.deepEqual(place.tags, ["주차 가능"]);
    assert.equal("raw" in place, false);
  } finally {
    globalThis.fetch = originalFetch;
  }
});

test("동기화 작업 claim은 service-role RPC에 원자 lease 인자를 보낸다", async () => {
  const originalUrl = process.env.SUPABASE_URL;
  const originalKey = process.env.SUPABASE_SERVICE_ROLE_KEY;
  const originalFetch = globalThis.fetch;
  process.env.SUPABASE_URL = "https://supabase.test";
  process.env.SUPABASE_SERVICE_ROLE_KEY = "service-role";
  let request;
  globalThis.fetch = async (url, init) => {
    request = { url: String(url), init };
    return Response.json(true);
  };
  try {
    const claimed = await claimSyncJob({
      jobId: "tour_intro",
      token: "run-1",
      now: "2026-08-28T00:00:00.000Z",
      leaseSeconds: 90
    });
    assert.match(request.url, /\/rest\/v1\/rpc\/claim_sync_job$/);
    assert.deepEqual(JSON.parse(request.init.body), {
      p_id: "tour_intro",
      p_token: "run-1",
      p_now: "2026-08-28T00:00:00.000Z",
      p_lease_seconds: 90
    });
    assert.ok(request.init.signal instanceof AbortSignal);
    assert.equal(claimed, true);
  } finally {
    globalThis.fetch = originalFetch;
    if (originalUrl === undefined) delete process.env.SUPABASE_URL;
    else process.env.SUPABASE_URL = originalUrl;
    if (originalKey === undefined) delete process.env.SUPABASE_SERVICE_ROLE_KEY;
    else process.env.SUPABASE_SERVICE_ROLE_KEY = originalKey;
  }
});

test("동기화 작업 finish는 소유 token과 결과 요약을 보낸다", async () => {
  const originalUrl = process.env.SUPABASE_URL;
  const originalKey = process.env.SUPABASE_SERVICE_ROLE_KEY;
  const originalFetch = globalThis.fetch;
  process.env.SUPABASE_URL = "https://supabase.test";
  process.env.SUPABASE_SERVICE_ROLE_KEY = "service-role";
  let request;
  globalThis.fetch = async (url, init) => {
    request = { url: String(url), init };
    return Response.json(false);
  };
  try {
    const finished = await finishSyncJob({
      jobId: "tour_intro",
      token: "run-1",
      status: "partial",
      summary: { processed: 4, failed: 1 },
      finishedAt: "2026-08-28T00:00:30.000Z"
    });
    assert.match(request.url, /\/rest\/v1\/rpc\/finish_sync_job$/);
    assert.deepEqual(JSON.parse(request.init.body), {
      p_id: "tour_intro",
      p_token: "run-1",
      p_status: "partial",
      p_summary: { processed: 4, failed: 1 },
      p_finished_at: "2026-08-28T00:00:30.000Z"
    });
    assert.ok(request.init.signal instanceof AbortSignal);
    assert.equal(finished, false);
  } finally {
    globalThis.fetch = originalFetch;
    if (originalUrl === undefined) delete process.env.SUPABASE_URL;
    else process.env.SUPABASE_URL = originalUrl;
    if (originalKey === undefined) delete process.env.SUPABASE_SERVICE_ROLE_KEY;
    else process.env.SUPABASE_SERVICE_ROLE_KEY = originalKey;
  }
});
