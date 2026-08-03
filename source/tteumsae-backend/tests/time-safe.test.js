import test from "node:test";
import assert from "node:assert/strict";
import { recommendPlaces, safetyLevel } from "../lib/time-safe.js";

const place = {
  content_id: "1",
  name: "테스트 카페",
  category: "CAFE",
  latitude: 37.75,
  longitude: 128.88,
  default_stay_minutes: 30
};

const criteria = {
  mode: "NEARBY",
  start: { latitude: 37.75, longitude: 128.88 },
  destination: { latitude: 37.75, longitude: 128.88 },
  deadlineMinutes: 70,
  safetyBufferMinutes: 15,
  transport: "WALK",
  categories: []
};

function fixedRoute() {
  return {
    firstLegMinutes: 10,
    secondLegMinutes: 10,
    directMinutes: 1,
    detourMinutes: 19,
    provider: "ESTIMATE"
  };
}

test("안전 여유시간 이상인 장소만 추천한다", () => {
  const result = recommendPlaces(criteria, [place], fixedRoute);
  assert.equal(result.length, 1);
  assert.equal(result[0].totalMinutes, 50);
  assert.equal(result[0].marginMinutes, 20);

  const none = recommendPlaces(
    { ...criteria, deadlineMinutes: 60, safetyBufferMinutes: 15 },
    [place],
    fixedRoute
  );
  assert.equal(none.length, 0);
});

test("선택된 카테고리를 적용한다", () => {
  const result = recommendPlaces(
    { ...criteria, categories: ["CULTURE"] },
    [place],
    fixedRoute
  );
  assert.equal(result.length, 0);
});

test("여유시간에 따라 안전도를 부여한다", () => {
  assert.equal(safetyLevel(20), "COMFORTABLE");
  assert.equal(safetyLevel(10), "AVAILABLE");
  assert.equal(safetyLevel(9), "TIGHT");
});

