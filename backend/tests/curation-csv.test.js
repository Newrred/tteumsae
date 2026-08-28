import test from "node:test";
import assert from "node:assert/strict";
import {
  CURATION_COLUMNS,
  parseCurationCsv,
  selectCurationCandidates,
  serializeCurationCsv,
  validateCurationRows
} from "../lib/curation-csv.js";

function validRow(overrides = {}) {
  return {
    content_id: "100",
    name: "강릉시립미술관",
    category: "CULTURE",
    operating_info_status: "VERIFIED",
    opening_hours: "09:00~18:00",
    closed_days: "매주 월요일",
    last_admission: "17:30",
    admission_info_status: "VERIFIED",
    parking_info: "건물 주차장 이용",
    parking_info_status: "VERIFIED",
    source_urls: '["https://www.gn.go.kr/place"]',
    source_checked_at: "2026-08-28T00:00:00.000Z",
    reviewed_at: "2026-08-28T00:00:00.000Z",
    review_note: "공식 페이지 확인",
    ...overrides
  };
}

test("CSV는 쉼표·따옴표·개행이 있는 검수 문구를 손실 없이 왕복한다", () => {
  const rows = [validRow({
    name: "박물관, 본관",
    review_note: "첫째 줄\n둘째 \"인용\" 줄"
  })];

  const csv = serializeCurationCsv(rows);
  const parsed = parseCurationCsv(csv.replace(/\n/g, "\r\n"));

  assert.deepEqual(Object.keys(parsed[0]), CURATION_COLUMNS);
  assert.equal(parsed[0].name, "박물관, 본관");
  assert.equal(parsed[0].review_note, "첫째 줄\r\n둘째 \"인용\" 줄");
});

test("정상 검수 행은 DB upsert 모델로 정규화한다", () => {
  const [row] = validateCurationRows([validRow()], { expectedCount: 1 });

  assert.equal(row.content_id, "100");
  assert.equal(row.operating_info_status, "VERIFIED");
  assert.deepEqual(row.source_urls, ["https://www.gn.go.kr/place"]);
  assert.equal(row.source_checked_at, "2026-08-28T00:00:00.000Z");
  assert.equal("name" in row, false);
  assert.equal("category" in row, false);
});

test("중복·상태·출처·시간·VERIFIED 필수값 오류를 행 번호와 함께 모아 낸다", () => {
  const rows = [
    validRow({ content_id: "same" }),
    validRow({
      content_id: "same",
      operating_info_status: "VERIFIED",
      opening_hours: "",
      admission_info_status: "BAD",
      parking_info_status: "VERIFIED",
      parking_info: "",
      source_urls: '["http://blog.example.com/place"]',
      source_checked_at: "not-a-date"
    })
  ];

  assert.throws(
    () => validateCurationRows(rows, { expectedCount: 2 }),
    (error) => {
      assert.match(error.message, /3행/);
      assert.match(error.message, /중복 content_id/);
      assert.match(error.message, /operating.*필수/i);
      assert.match(error.message, /admission_info_status/);
      assert.match(error.message, /HTTPS/);
      assert.match(error.message, /source_checked_at/);
      return true;
    }
  );
});

test("정확한 목표 행 수를 강제하고 부분 검수는 완료 행만 엄격 검증한다", () => {
  assert.throws(
    () => validateCurationRows([validRow()], { expectedCount: 100 }),
    /100개여야/
  );

  const unreviewed = validRow({
    content_id: "200",
    source_urls: "[]",
    source_checked_at: "",
    reviewed_at: "",
    operating_info_status: "UNKNOWN",
    opening_hours: "",
    closed_days: "",
    last_admission: "",
    admission_info_status: "UNKNOWN",
    parking_info: "",
    parking_info_status: "UNKNOWN"
  });
  const rows = validateCurationRows(
    [validRow(), unreviewed],
    { expectedCount: 2, allowPartial: true }
  );
  assert.equal(rows.length, 1);
  assert.equal(rows[0].content_id, "100");
});

test("후보 선정은 품질순을 유지하면서 카테고리를 순환해 100개를 고른다", () => {
  const candidates = Array.from({ length: 105 }, (_, index) => ({
    content_id: String(index + 1),
    name: `장소 ${String(index).padStart(3, "0")}`,
    category: index < 100 ? "ATTRACTION" : "CULTURE",
    image_url: index % 2 === 0 ? "https://example.com/image.jpg" : null,
    overview: index % 3 === 0 ? "소개" : null,
    intro_synced_at: index % 5 === 0 ? "2026-08-28T00:00:00Z" : null
  }));

  const selected = selectCurationCandidates(candidates, 100);

  assert.equal(selected.length, 100);
  assert.equal(new Set(selected.map((row) => row.content_id)).size, 100);
  assert.ok(selected.some((row) => row.category === "CULTURE"));
  assert.equal(selected[0].image_url, "https://example.com/image.jpg");
});
