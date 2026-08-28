import test from "node:test";
import assert from "node:assert/strict";
import * as tourApi from "../lib/tour-api.js";

const { mapTourItem, normalizeTourEnrichment } = tourApi;

test("TourAPI 관광지를 DB 행으로 변환한다", () => {
  const result = mapTourItem(
    {
      contentid: "123",
      contenttypeid: "12",
      title: "강릉 테스트 장소",
      areacode: "32",
      sigungucode: "1",
      mapx: "128.8761",
      mapy: "37.7519",
      addr1: "강원특별자치도 강릉시",
      firstimage: "https://example.com/image.jpg",
      modifiedtime: "20260723"
    },
    "2026-07-23T00:00:00.000Z"
  );

  assert.equal(result.content_id, "123");
  assert.equal(result.category, "ATTRACTION");
  assert.equal(result.default_stay_minutes, 60);
  assert.equal(result.longitude, 128.8761);
});

test("지원하지 않는 숙박 타입은 제외한다", () => {
  const result = mapTourItem({
    contentid: "456",
    contenttypeid: "32",
    title: "숙박시설",
    mapx: "128.8",
    mapy: "37.7"
  });
  assert.equal(result, null);
});

test("상세정보를 카드 태그와 대표 이미지 후보로 정규화한다", () => {
  const result = normalizeTourEnrichment({
    contentTypeId: 14,
    intro: {
      parkingculture: "주차 가능",
      chkbabycarriageculture: "없음",
      usetimeculture: "09:00~18:00<br>입장 마감 17:30",
      restdateculture: "매주 월요일"
    },
    images: [
      {
        originimgurl: "https://example.com/original.jpg",
        smallimageurl: "https://example.com/thumb.jpg"
      }
    ],
    pet: { acmpyTypeCd: "동반 가능" }
  });

  assert.deepEqual(result.tags, ["주차 가능", "반려동물 동반", "실내 활동"]);
  assert.deepEqual(result.imageUrls, [
    "https://example.com/original.jpg",
    "https://example.com/thumb.jpg"
  ]);
  assert.equal(result.openingHours, "09:00~18:00 입장 마감 17:30");
  assert.equal(result.closedDays, "매주 월요일");
});

test("TourAPI 카페 소분류만 CAFE로 매핑한다", () => {
  const base = {
    contentid: "1",
    contenttypeid: "39",
    title: "테스트",
    areacode: "32",
    mapx: "128.87",
    mapy: "37.75",
    cat1: "A05",
    cat2: "A0502"
  };

  const cafe = mapTourItem({ ...base, cat3: "A05020900" });
  const restaurant = mapTourItem({ ...base, cat3: "A05020100" });
  const uncategorized = mapTourItem({ ...base, cat3: "" });

  assert.equal(cafe.category, "CAFE");
  assert.equal(cafe.cat1, "A05");
  assert.equal(cafe.cat2, "A0502");
  assert.equal(cafe.cat3, "A05020900");
  assert.equal(restaurant.category, "RESTAURANT");
  assert.equal(uncategorized.category, "RESTAURANT");
});

test("인트로에서 영업시간과 행사 기간을 분리한다", () => {
  const result = tourApi.normalizeTourIntro({
    contentTypeId: 15,
    intro: {
      playtime: "10:00~18:00<br>",
      eventstartdate: "20260820",
      eventenddate: "20260831"
    },
    syncedAt: "2026-08-27T00:00:00.000Z"
  });

  assert.equal(result.openingHours, "10:00~18:00");
  assert.equal(result.eventStartDate, "2026-08-20");
  assert.equal(result.eventEndDate, "2026-08-31");
  assert.equal(result.syncedAt, "2026-08-27T00:00:00.000Z");
});

test("인트로의 잘못된 행사 날짜는 저장하지 않는다", () => {
  const result = tourApi.normalizeTourIntro({
    contentTypeId: 15,
    intro: {
      eventstartdate: "2026-08-20",
      eventenddate: "20260230"
    },
    syncedAt: "2026-08-27T00:00:00.000Z"
  });

  assert.equal(result.eventStartDate, null);
  assert.equal(result.eventEndDate, null);
});

test("공통 상세의 소개와 홈페이지를 정규화한다", () => {
  const result = tourApi.normalizeTourCommon({
    common: {
      overview: "<p>바다 옆 <b>전시관</b></p>",
      homepage: '<a href="https://example.com/place">홈페이지</a>'
    },
    syncedAt: "2026-08-27T00:00:00.000Z"
  });

  assert.equal(result.overview, "바다 옆 전시관");
  assert.equal(result.homepageUrl, "https://example.com/place");
  assert.equal(result.syncedAt, "2026-08-27T00:00:00.000Z");
});

test("공통 상세 홈페이지는 HTTP 계열 주소만 허용한다", () => {
  const result = tourApi.normalizeTourCommon({
    common: {
      overview: null,
      homepage: '<a href="javascript:alert(1)">홈페이지</a>'
    },
    syncedAt: "2026-08-27T00:00:00.000Z"
  });

  assert.equal(result.homepageUrl, null);
});

test("미디어 상세는 이미지와 표현 태그만 정규화한다", () => {
  const result = tourApi.normalizeTourMedia({
    contentTypeId: 14,
    intro: { parkingculture: "주차 가능" },
    images: [
      {
        originimgurl: "https://example.com/original.jpg",
        smallimageurl: "https://example.com/original.jpg"
      }
    ],
    pet: { acmpyTypeCd: "동반 가능" },
    syncedAt: "2026-08-27T00:00:00.000Z"
  });

  assert.deepEqual(result.tags, ["주차 가능", "반려동물 동반", "실내 활동"]);
  assert.deepEqual(result.imageUrls, ["https://example.com/original.jpg"]);
  assert.equal(result.syncedAt, "2026-08-27T00:00:00.000Z");
});

test("공통 상세 조회는 현재 KorService2 계약과 단일 객체 응답을 처리한다", async () => {
  const originalFetch = globalThis.fetch;
  const originalServiceKey = process.env.TOUR_API_SERVICE_KEY;
  process.env.TOUR_API_SERVICE_KEY = "service-key";
  let requestUrl;
  globalThis.fetch = async (url) => {
    requestUrl = new URL(String(url));
    return Response.json({
      response: {
        header: { resultCode: "0000", resultMsg: "OK" },
        body: {
          numOfRows: 10,
          pageNo: 1,
          totalCount: 1,
          items: {
            item: {
              contentid: "123",
              contenttypeid: "12",
              title: "테스트 장소",
              overview: "<p>소개</p>",
              homepage: "https://example.com"
            }
          }
        }
      }
    });
  };

  try {
    const result = await tourApi.fetchTourCommon("123");
    assert.equal(result.contentid, "123");
    assert.equal(requestUrl.pathname.endsWith("/detailCommon2"), true);
    assert.equal(requestUrl.searchParams.get("contentId"), "123");
    assert.equal(requestUrl.searchParams.get("numOfRows"), "10");
    assert.equal(requestUrl.searchParams.get("pageNo"), "1");
    assert.equal(requestUrl.searchParams.has("defaultYN"), false);
    assert.equal(requestUrl.searchParams.has("overviewYN"), false);
  } finally {
    globalThis.fetch = originalFetch;
    if (originalServiceKey === undefined) delete process.env.TOUR_API_SERVICE_KEY;
    else process.env.TOUR_API_SERVICE_KEY = originalServiceKey;
  }
});

test("TourAPI 목록·상세·증분 요청은 모두 timeout signal을 전달한다", async () => {
  const originalFetch = globalThis.fetch;
  const originalServiceKey = process.env.TOUR_API_SERVICE_KEY;
  process.env.TOUR_API_SERVICE_KEY = "service-key";
  const signals = [];
  const fetchImpl = async (url, options) => {
    signals.push(options.signal);
    const pathname = new URL(String(url)).pathname;
    const item = pathname.endsWith("/detailCommon2")
      ? { contentid: "123", overview: "소개" }
      : {
          contentid: "123",
          contenttypeid: "12",
          title: "테스트 장소",
          mapx: "128.87",
          mapy: "37.75",
          showflag: "1"
        };
    return Response.json({
      response: {
        header: { resultCode: "0000", resultMsg: "OK" },
        body: {
          pageNo: 1,
          numOfRows: 100,
          totalCount: 1,
          items: { item: [item] }
        }
      }
    });
  };
  globalThis.fetch = fetchImpl;

  try {
    await tourApi.fetchTourPage(1, 100, { fetchImpl });
    await tourApi.fetchTourCommon("123", { fetchImpl });
    await tourApi.fetchTourSyncPage({ pageNo: 1, fetchImpl });
    assert.equal(signals.length, 3);
    assert.ok(signals.every((signal) => signal instanceof AbortSignal));
  } finally {
    globalThis.fetch = originalFetch;
    if (originalServiceKey === undefined) delete process.env.TOUR_API_SERVICE_KEY;
    else process.env.TOUR_API_SERVICE_KEY = originalServiceKey;
  }
});
