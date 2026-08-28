import test from "node:test";
import assert from "node:assert/strict";
import { createOpsStatusHandler } from "../api/ops/status.js";

const fixedNow = new Date("2026-08-28T00:00:00.000Z");

function request(method = "GET", token) {
  return new Request("https://example.test/api/ops/status", {
    method,
    headers: token ? { authorization: `Bearer ${token}` } : {}
  });
}

test("운영 상태 API는 GET만 허용하고 인증 전에 DB에 접근하지 않는다", async () => {
  let calls = 0;
  const handler = createOpsStatusHandler({
    secret: () => "cron-secret",
    now: () => fixedNow,
    getStatus: async () => {
      calls += 1;
      return {};
    }
  });

  assert.equal((await handler.fetch(request("POST", "cron-secret"))).status, 405);
  assert.equal((await handler.fetch(request())).status, 401);
  assert.equal((await handler.fetch(request("GET", "wrong"))).status, 401);
  assert.equal(calls, 0);
});

test("인증된 운영 상태 API는 KST 날짜와 강릉 100개 목표를 조회한다", async () => {
  let input;
  const handler = createOpsStatusHandler({
    secret: () => "cron-secret",
    now: () => fixedNow,
    getStatus: async (value) => {
      input = value;
      return {
        usage: [{ provider: "KAKAO_MOBILITY", reservedCount: 7_000 }],
        syncJobs: [{ id: "tour_intro", lastStatus: "completed" }],
        dataQuality: { curation: { target: 100, reviewed: 12 } }
      };
    }
  });

  const response = await handler.fetch(request("GET", "cron-secret"));
  const body = await response.json();

  assert.equal(response.status, 200);
  assert.equal(response.headers.get("cache-control"), "no-store");
  assert.deepEqual(input, {
    usageDate: "2026-08-28",
    sigunguCode: 1,
    curationTarget: 100
  });
  assert.equal(body.status, "ok");
  assert.equal(body.generatedAt, fixedNow.toISOString());
  assert.equal(body.usageDate, "2026-08-28");
  assert.equal(body.usage[0].reservedCount, 7_000);
  assert.equal(body.dataQuality.curation.reviewed, 12);
});
