# API 및 데이터 설계

작성 기준: 2026-08-27 통합 소스
백엔드 패키지 버전: `0.2.0`
운영 API 기준 주소: `https://tteumsae-backend.vercel.app`

이 문서는 현재 구현된 HTTP 계약, 외부 API 연동, Supabase 스키마와
TourAPI 동기화 과정을 설명한다. 기획상 예정된 동작이 아니라 실제
[`backend/`](../backend/)와 Android 파서
[`TteumsaeApi.kt`](../android/app/src/main/java/com/tteumsae/app/data/TteumsaeApi.kt)를
기준으로 작성했다.

## 1. 전체 데이터 흐름

```text
Android 앱
  ├─ 선택 로그인·프로필 ─────> Supabase Auth / profiles(RLS)
  ├─ 위치 검색 ──────────────> GET /api/geocode ──> Kakao Local 키워드 검색
  ├─ 행정구역 확인/역지오코딩 > GET /api/region ───> Kakao Local 좌표→행정구역
  ├─ 틈새 발견 목록 ─────────> GET /api/places ────> Supabase places
  ├─ 추천 요청 ──────────────> POST /api/recommendations
  │                                 ├─ Kakao Mobility 출발→목적 baseRoute
  │                                 ├─ baseRoute corridor의 Supabase 후보 조회
  │                                 └─ 후보별 출발→후보→목적 경로
  └─ 경유지 변경 ────────────> POST /api/route
                                    └─ Kakao Mobility 출발→경유지 0~5→목적

Vercel Cron
  ├─ /api/cron/tour-sync ───────> TourAPI areaBasedList2 → Supabase
  └─ /api/cron/tour-detail-sync > TourAPI 상세 3종 → places.raw._tteumsae
```

중요한 경계는 다음과 같다.

- Android 앱에는 TourAPI 키, Kakao REST API 키, Supabase 서비스 역할 키가 없다.
- 장소·추천·경로 기능은 Android가 Vercel 공개 API를 통해서만 호출한다.
- 예외적으로 선택 로그인 세션과 본인 `profiles` 행은 Supabase 공개 클라이언트와
  RLS를 통해 Android가 직접 처리한다. 장소·추천 데이터는 계속 Vercel만 통한다.
- Kakao Maps **네이티브 앱 키**만 Android 빌드 시 주입된다.
- Android에는 Supabase publishable key만 있고 service role key는 없다.
- 추천 API는 직행 `baseRoute`를 먼저 계산한 뒤 그 경로 주변에서 후보를 찾고,
  후보 한 곳을 경유하는 경로를 장소별로 계산한다.
- 결과 화면에서 경유지를 추가·제거할 때 Android가 `POST /api/route`를 호출한다.
  선택 순서의 0~5개 경유지를 포함한 통합 시간·거리·통행료·path를 Kakao
  Mobility에서 다시 계산한다.
- 현재 활성 UI에는 시간 입력 단계가 없다. Android는 넓은 경로 주변 후보 탐색을
  위한 내부 호환값 `extraTimeMinutes=1,440`, `safetyBufferMinutes=15`를 보낸다.
  이는 사용자의 실제 도착 마감이나 보장 시간이 아니다.

## 2. 공통 HTTP 규칙

### 2.1 응답 헤더

[`backend/lib/http.js`](../backend/lib/http.js)의 모든 정상·오류 JSON 응답은
다음을 사용한다.

```http
Content-Type: application/json; charset=utf-8
Cache-Control: no-store
```

### 2.2 공통 오류 형태

```json
{
  "error": {
    "code": "BAD_REQUEST",
    "message": "사용자에게 표시할 수 있는 메시지",
    "details": null
  }
}
```

| HTTP | `error.code` | 현재 동작 |
|---:|---|---|
| 400 | `BAD_REQUEST` | 파라미터 또는 JSON 본문 검증 실패 |
| 401 | `UNAUTHORIZED` | Cron Bearer 인증 실패 |
| 404 | `NOT_FOUND` | 요청한 장소가 없을 때 |
| 405 | `METHOD_NOT_ALLOWED` | 허용하지 않은 메서드. `Allow` 헤더 포함 |
| 500 | `INTERNAL_ERROR` | 외부 API·DB·환경설정 등 서버 오류 |

500 응답은 외부 서비스 본문이나 비밀값을 노출하지 않고 무작위 `requestId`만
돌려준다.

```json
{
  "error": {
    "code": "INTERNAL_ERROR",
    "message": "서버 처리 중 오류가 발생했습니다.",
    "requestId": "UUID"
  }
}
```

실제 오류 메시지는 Vercel Function 로그에서 같은 `requestId`로 확인한다.
Android는 비정상 HTTP 응답의 `error.message`를 우선 표시하고, JSON 해석에
실패하면 `서버 요청에 실패했습니다.`를 표시한다. 연결·타임아웃 등
`IOException`은 `네트워크 연결을 확인해주세요.`로 바뀐다.

### 2.3 공개 범위

공개 API에는 사용자 인증이 없다. 고비용 `/api/recommendations`는 IP별 분당 12회,
`/api/route`는 IP별 분당 40회의 인스턴스 메모리 제한을 적용하고 초과 시
`429 RATE_LIMITED`와 `Retry-After`를 반환한다. 이 제한은 Vercel 인스턴스별
best-effort라 분산 요청이나 IP 헤더가 없는 요청을 완전히 막지 못한다. Cron 두 개만
`Authorization: Bearer {CRON_SECRET}`을 요구한다. 공개 규모가 커지면 공유 저장소나
Vercel 경계 정책으로 교체해야 한다.

## 3. 엔드포인트 요약

| Method | Path | 인증 | 용도 |
|---|---|---|---|
| GET | `/api/health` | 없음 | 서버 버전과 연동 설정 존재 여부 |
| GET | `/api/places` | 없음 | 활성 TourAPI 장소 페이지 조회 |
| GET | `/api/places/{contentId}` | 없음 | 활성 장소 한 건 조회 |
| GET | `/api/geocode` | 없음 | 카카오 키워드 장소 검색 |
| GET | `/api/region` | 없음 | 좌표의 행정구역 및 강원도 여부 |
| POST | `/api/recommendations` | 없음 | 시간·경로·카테고리 기반 추천 |
| POST | `/api/route` | 없음 | 출발·최종 목적지와 경유지 0~5개의 통합 차량 경로 재계산 |
| DELETE | `/api/account` | Supabase Bearer | 현재 로그인 사용자의 Auth 계정과 연관 사용자 데이터 영구 삭제 |
| GET | `/api/cron/tour-sync` | Bearer | TourAPI 기본 장소 페이지 동기화 |
| GET | `/api/cron/tour-detail-sync` | Bearer | 이미지·편의·운영정보 보강 |

## 4. 장소 데이터 계약

### 4.1 앱에 공개되는 장소 객체

`GET /api/places`, `GET /api/places/{id}`와 추천 응답의 `place`는 다음
필드를 사용한다.

| 필드 | 타입 | nullable | 의미 |
|---|---|---:|---|
| `content_id` | string | 아니오 | TourAPI 콘텐츠 ID, DB 기본키 |
| `source` | string | 아니오 | 현재 항상 `TOUR_API` |
| `name` | string | 아니오 | 장소명 |
| `category` | string | 아니오 | 틈새 내부 카테고리 |
| `content_type_id` | integer | 아니오 | TourAPI 콘텐츠 유형 |
| `area_code` | integer | 아니오 | 현재 강원도 `32` |
| `sigungu_code` | integer | 예 | TourAPI 시군구 코드 |
| `latitude` | number | 아니오 | 위도 |
| `longitude` | number | 아니오 | 경도 |
| `address` | string | 예 | `addr1`과 `addr2` 결합 |
| `image_url` | string | 예 | 기본 대표 이미지 또는 상세 이미지 첫 항목 |
| `tel` | string | 예 | TourAPI 전화번호 |
| `default_stay_minutes` | integer | 아니오 | 카테고리 기본 머무름 시간 |
| `image_urls` | string[] | 아니오 | 상세 이미지 URL 목록, 없으면 빈 배열 |
| `tags` | string[] | 아니오 | 정제한 편의 태그, 없으면 빈 배열 |
| `opening_hours` | string | 예 | TourAPI 원문을 HTML 제거·공백 정리한 값 |
| `closed_days` | string | 예 | TourAPI 원문을 HTML 제거·공백 정리한 값 |

DB의 `raw`, `is_active`, `source_modified_at`, `synced_at` 필드는 공개 응답에서
제외된다.

현재 내부 카테고리는 다음 일곱 개다.

```text
ATTRACTION, RESTAURANT, CAFE, CULTURE, FESTIVAL, SHOPPING, LEISURE
```

다만 현재 TourAPI 기본 동기화는 별도 카페 콘텐츠 유형을 매핑하지 않는다.
음식점 콘텐츠 유형 `39`는 모두 `RESTAURANT`로 저장하므로 DB에 `CAFE`가 거의
없거나 없을 수 있다. 카페 추천 품질 개선 시 분류 규칙 또는 별도 데이터 소스가
필요하다.

### 4.2 콘텐츠 유형과 기본 머무름

[`backend/lib/tour-api.js`](../backend/lib/tour-api.js)의 현재 고정값이다.

| TourAPI `contenttypeid` | 내부 카테고리 | 기본 머무름 |
|---:|---|---:|
| 12 | `ATTRACTION` | 60분 |
| 14 | `CULTURE` | 90분 |
| 15 | `FESTIVAL` | 60분 |
| 28 | `LEISURE` | 60분 |
| 38 | `SHOPPING` | 40분 |
| 39 | `RESTAURANT` | 40분 |

숙박 등 이 표에 없는 콘텐츠 유형, ID·제목·좌표가 없는 항목은 DB 행으로
변환하지 않는다.

## 5. 공개 API 상세

### 5.1 `GET /api/health`

환경변수의 **존재 여부만** 확인한다. 외부 API나 DB에 실제 요청을 보내지
않으므로 `true`가 연동 정상까지 보장하지는 않는다.

```json
{
  "status": "ok",
  "service": "tteumsae-backend",
  "version": "0.2.0",
  "timestamp": "2026-08-16T00:00:00.000Z",
  "integrations": {
    "tourApiConfigured": true,
    "databaseConfigured": true,
    "kakaoRoutingConfigured": true
  }
}
```

### 5.2 `GET /api/places`

쿼리:

| 이름 | 기본값 | 제한 | 설명 |
|---|---:|---:|---|
| `page` | 1 | 1 이상 정수 | 페이지 번호 |
| `pageSize` | 30 | 1~100 | 한 페이지 항목 수 |
| `category` | 없음 | 내부 카테고리 7종 | 대소문자 무관 |
| `sigunguCode` | 없음 | 1~18 정수 | TourAPI 강원 시군구 코드. 생략하면 강원도 전체 |

정렬은 장소명 오름차순이고 `is_active=true`인 행만 반환한다.
`sigunguCode`를 전달하면 DB의 `sigungu_code`가 정확히 같은 행만 서버에서
필터링한다. 지원 범위는 강릉 1, 고성 2, 동해 3, 삼척 4, 속초 5, 양구 6,
양양 7, 영월 8, 원주 9, 인제 10, 정선 11, 철원 12, 춘천 13, 태백 14,
평창 15, 홍천 16, 화천 17, 횡성 18이다.

```http
GET /api/places?page=1&pageSize=100&category=ATTRACTION&sigunguCode=1
```

```json
{
  "data": [
    {
      "content_id": "123456",
      "source": "TOUR_API",
      "name": "예시 관광지",
      "category": "ATTRACTION",
      "content_type_id": 12,
      "area_code": 32,
      "sigungu_code": 1,
      "latitude": 37.75,
      "longitude": 128.88,
      "address": "강원특별자치도 강릉시 예시로 1",
      "image_url": "https://example.invalid/image.jpg",
      "tel": null,
      "default_stay_minutes": 60,
      "image_urls": [],
      "tags": ["주차 가능"],
      "opening_hours": null,
      "closed_days": null
    }
  ],
  "pagination": {
    "page": 1,
    "pageSize": 100,
    "returned": 1,
    "hasMore": false
  }
}
```

`hasMore`는 `returned === pageSize`인지로만 계산하며 전체 개수는 제공하지
않는다. 마지막 페이지가 정확히 `pageSize`개이면 다음 빈 페이지를 요청할 수
있다. `sigunguCode`가 정수가 아니거나 1~18 밖이면 400
`지원하지 않는 강원도 지역입니다.`를 반환한다. 숫자가 아닌 `page`/`pageSize`의
명시적 400 검증은 현재 없어서 DB 오류로 500이 될 수 있다.

Android 틈새 발견 화면은 최초 `강릉`을 선택해 `sigunguCode=1`을 요청한다.
지역을 바꾸면 페이지를 1로 초기화하고 이후 추가 페이지에도 같은 코드를
유지하며, `강원도 전체`에서는 코드를 생략한다. 서버 카탈로그는 코드로
필터링하지만 Android가 로컬에 저장한 장소 사본에는 `sigungu_code`가 없으므로,
이 사본은 주소에 선택 지역명이 포함되는지로 보완 필터한다. 주소가 비어 있거나
표기가 다른 경우 로컬 찜이 해당 지역 목록에서 누락될 수 있다.

### 5.3 `GET /api/places/{contentId}`

활성 장소 한 건을 `{ "data": 장소 }`로 반환한다. ID가 없거나 비활성·미존재면
404 `NOT_FOUND`다.

### 5.4 `GET /api/geocode`

Kakao Local 키워드 검색을 서버에서 대행한다.

| 이름 | 필수 | 규칙 |
|---|---:|---|
| `q` | 예 | 앞뒤 공백 제거 후 2~100자 |
| `latitude` | 아니오 | -90~90, `longitude`와 함께 전달 |
| `longitude` | 아니오 | -180~180, `latitude`와 함께 전달 |

좌표가 있으면 해당 지점 중심 20km 안에서 거리순으로, 없으면 정확도순으로
최대 10건을 요청한다.

```json
{
  "data": [
    {
      "id": "카카오 장소 ID",
      "name": "강릉역",
      "address": "강원특별자치도 강릉시 용지로 176",
      "category": "교통,수송 > 기차역",
      "latitude": 37.764,
      "longitude": 128.899,
      "kakaoMapUrl": "https://place.map.kakao.com/..."
    }
  ],
  "meta": {
    "query": "강릉역",
    "resultCount": 1,
    "provider": "KAKAO_LOCAL"
  }
}
```

Android의 `searchPlaces(query, gangwonOnly=true)`는 검색어에 `강원`이 없으면
자동으로 `강원 ` 접두어를 붙인다. 현재 Android는 응답 중 `id`, `name`,
`address`, 좌표만 사용한다.

### 5.5 `GET /api/region`

```http
GET /api/region?latitude=37.7519&longitude=128.8761
```

Kakao Local 좌표→행정구역 응답에서 행정동(`region_type=H`)을 우선하고,
없으면 첫 문서를 사용한다.

```json
{
  "data": {
    "province": "강원특별자치도",
    "address": "강원특별자치도 강릉시 ...",
    "isGangwon": true
  }
}
```

`isGangwon`은 시도명이 `강원`으로 시작하는지만 판정한다. Android는 근처 탐색
가능 지역 확인과 현재 좌표의 지명 표시에 이 API를 사용한다.

### 5.6 `POST /api/recommendations`

요청은 반드시 `Content-Type: application/json`이어야 한다.

```json
{
  "mode": "ON_THE_WAY",
  "start": { "latitude": 37.7519, "longitude": 128.8761 },
  "destination": { "latitude": 37.7644, "longitude": 128.8996 },
  "extraTimeMinutes": 90,
  "safetyBufferMinutes": 15,
  "transport": "CAR",
  "categories": ["ATTRACTION", "CULTURE"]
}
```

| 필드 | 규칙 |
|---|---|
| `mode` | `ON_THE_WAY` 또는 `NEARBY` |
| `start` | 유효한 위도·경도 숫자 |
| `destination` | 유효한 위도·경도 숫자. `NEARBY`도 현재 필수 |
| `extraTimeMinutes` | Android가 사용하는 순수 경유 여유시간. 15~1440분 정수 |
| `deadlineMinutes` | 이전 클라이언트 호환용 전체 시간 예산. 15~1440분 정수 |
| `safetyBufferMinutes` | 0~60분 정수이며 요청에 포함한 시간 필드보다 작음 |
| `transport` | `CAR` 또는 `WALK` |
| `categories` | 내부 카테고리 배열. 생략/빈 배열이면 전체 |

`deadlineMinutes`와 `extraTimeMinutes`는 정확히 하나만 보내야 한다. 현재 Android
활성 흐름은 `ON_THE_WAY`, `CAR`, 사용자가 입력한 `extraTimeMinutes`, 안전여유와
선택 카테고리를 보낸다.

응답 예시:

```json
{
  "baseRoute": {
    "provider": "KAKAO_MOBILITY",
    "waypointCount": 0,
    "totalDrivingMinutes": 25,
    "totalDistanceMeters": 15100,
    "tollFareWon": 0,
    "legs": [{ "drivingMinutes": 25, "distanceMeters": 15100 }],
    "path": [
      { "latitude": 37.7519, "longitude": 128.8761 },
      { "latitude": 37.7644, "longitude": 128.8996 }
    ]
  },
  "data": [
    {
      "place": { "content_id": "123456", "name": "예시 관광지" },
      "route": {
        "firstLegMinutes": 18,
        "secondLegMinutes": 22,
        "directMinutes": 25,
        "detourMinutes": 15,
        "firstLegDistanceMeters": 8200,
        "secondLegDistanceMeters": 10100,
        "totalDistanceMeters": 18300,
        "path": [
          { "latitude": 37.75, "longitude": 128.87 },
          { "latitude": 37.76, "longitude": 128.89 }
        ],
        "provider": "KAKAO_MOBILITY"
      },
      "stayMinutes": 60,
      "totalMinutes": 100,
      "marginMinutes": 15,
      "operationStatus": "OPEN",
      "safetyLevel": "AVAILABLE"
    }
  ],
  "meta": {
    "candidateCount": 200,
    "corridorCandidateCount": 80,
    "routeCandidateCount": 20,
    "routeFailureCount": 0,
    "recommendationCount": 20,
    "routeProvider": "KAKAO_MOBILITY",
    "baseRouteMinutes": 25,
    "extraTimeMinutes": 90,
    "effectiveDeadlineMinutes": 115,
    "safetyBufferMinutes": 15,
    "corridorRadiusMeters": 1800,
    "baseRoute": { "waypointCount": 0, "provider": "KAKAO_MOBILITY" }
  }
}
```

`baseRoute`는 `CAR + ON_THE_WAY`일 때만 최상위와 `meta`에 모두 포함된다.
Android는 최상위 객체를 `RouteSummary`로 파싱하고 `meta.corridorRadiusMeters`를
결과 지도에 사용한다. `RouteSummary`는 provider, waypointCount,
totalDrivingMinutes, totalDistanceMeters, tollFareWon, legs, path를 보존한다.

`WALK`이면 경로 객체에 거리·path가 없고 `provider`는 `ESTIMATE`다. 또한
`meta.warning`에 도보 시간이 직선거리 기반 예상값이라는 안내가 포함된다.

#### 후보 조회와 상한

`CAR + ON_THE_WAY`:

1. Kakao Mobility에 경유지 없는 출발→목적 `baseRoute`를 요청한다.
2. `effectiveDeadlineMinutes = baseRoute.durationMinutes + extraTimeMinutes`를
   계산한다. corridor 반경은 `extraTimeMinutes × 20m`이며 800~8000m로
   제한한다.
3. `baseRoute.path`의 위·경도 bounds를 corridor만큼 넓혀 활성 장소를 최대
   500개 읽는다.
4. 각 장소에서 path 선분까지의 로컬 평면 근사 거리가 corridor 이내인 것만
   남긴다.
5. 카테고리를 적용하고 추정 우회시간이 짧은 최대 20개를 고른다.
6. 각 후보에 출발→후보 1곳→목적 Kakao 경로를 요청하고 시간 조건을 통과한
   최대 20개를 반환한다.

그 밖의 모드/이동수단은 출발·목적 사각형에 자동차 약 ±0.22도, 도보 약
±0.055도 패딩을 더한 기존 bounds를 사용한다.

따라서 `candidateCount`는 bounds에서 읽은 최대 500개의 개수,
`corridorCandidateCount`는 path 거리 필터 후 개수다. 둘 다 DB 전체 개수가
아니다.

#### 시간 공식

```text
effectiveDeadlineMinutes = baseRouteMinutes + extraTimeMinutes
우회 주행시간 = 후보 경유 주행시간 - baseRouteMinutes
남는 순수 여유 = extraTimeMinutes - (우회 주행시간 + 기본 머무름)
추천 조건 = 우회 주행시간 + 기본 머무름 + safetyBufferMinutes
            <= extraTimeMinutes
```

안전도:

| 남는 시간 | `safetyLevel` |
|---:|---|
| 20분 이상 | `COMFORTABLE` |
| 10~19분 | `AVAILABLE` |
| 0~9분 | `TIGHT` |

운영시간은 장소 도착 예상시각부터 기본 머무름이 끝나는 시각까지를 기준으로
단순 파싱한다. 그 사이 명확히 휴무/영업 종료로 판정된 `CLOSED` 장소는
제외한다. 데이터가 없거나 복잡해서 해석하지 못하면
`UNKNOWN`으로 추천에 남기며 앱이 `운영시간 확인 필요`를 표시한다.

`marginMinutes`는 위 식의 `남는 순수 여유`이며 사용자가 입력한 시간에서
우회와 머무름을 제외하고 남은 값이다. 교통과 실제 체류시간이 바뀔 수 있으므로
여전히 `100% 도착 보장`으로 표현하지 않는다.

#### 자동차 경로의 현재 한계

- 장소마다 `origin → waypoint 1개 → destination`을 요청한다.
- 후보별 요청은 동시성 5로 실행한다.
- 일부 실패는 제외하고 `routeFailureCount`에 넣는다.
- 후보가 있는데 모든 경로가 실패하면 추천 API 전체가 500이다.
- `directMinutes`는 앞에서 받은 Kakao `baseRoute.durationMinutes`로 덮어쓰며,
  후보의 `detourMinutes`는 후보 경유 경로와 baseRoute의 차이다.
- 후보별 계산은 한 곳만 경유한다. 여러 후보 조합은 아래 `/api/route`가 별도로
  계산한다.

#### 도보 경로의 현재 한계

```text
속도 4.5km/h × 직선거리 × 도로계수 1.2
```

에 기반한 예상값이다. 실제 보행로, 횡단보도, 경사, 통행 제한을 반영하지 않는다.
앱과 문구에서 100% 시간 보장을 표현하면 안 된다.

### 5.7 `POST /api/route`

결과 화면에서 경유지를 추가하거나 제거할 때 호출하는 차량 전용 통합 경로
계산 API다.

```json
{
  "start": { "latitude": 37.7519, "longitude": 128.8761 },
  "destination": { "latitude": 37.7644, "longitude": 128.8996 },
  "waypoints": [
    {
      "contentId": "123456",
      "latitude": 37.758,
      "longitude": 128.887
    }
  ]
}
```

| 필드 | 규칙 |
|---|---|
| `start`, `destination` | 유효한 위도·경도 숫자 |
| `waypoints` | 생략 가능, 배열 0~5개, 각 항목에 유효한 좌표 필수 |
| `contentId` | Android가 함께 보내지만 현재 서버 검증/경로 요청은 좌표만 사용 |

6개 이상, 좌표 범위 오류, 잘못된 JSON은 Kakao 호출 전에 400으로 거부한다.
서버는 waypoint 배열 순서를 그대로 Kakao `waypoints=lon,lat|...`에 전달하며
순서를 최적화하지 않는다.

성공 응답:

```json
{
  "data": {
    "provider": "KAKAO_MOBILITY",
    "waypointCount": 1,
    "durationMinutes": 38,
    "distanceMeters": 19200,
    "tollFare": 0,
    "totalDrivingMinutes": 38,
    "totalDistanceMeters": 19200,
    "tollFareWon": 0,
    "legs": [
      { "drivingMinutes": 17, "distanceMeters": 8000 },
      { "drivingMinutes": 21, "distanceMeters": 11200 }
    ],
    "path": [
      { "latitude": 37.7519, "longitude": 128.8761 },
      { "latitude": 37.758, "longitude": 128.887 },
      { "latitude": 37.7644, "longitude": 128.8996 }
    ]
  }
}
```

`path`는 Kakao road vertex를 출발·목적과 합친 뒤 최대 200점으로 균등
샘플링한다. `legs`는 경유지 수+1개여야 하며 Kakao 응답이 이 계약을 만족하지
않으면 서버가 500을 반환한다. Android는 실패 시 단일 후보 우회값을 합친
`ESTIMATE` fallback을 표시하고 토스트를 띄운다. fallback은 Kakao 통합
경로와 동급 정확도가 아니다.

### 5.8 `DELETE /api/account`

선택형 로그인 사용자의 계정을 영구 삭제한다. 요청은
`Authorization: Bearer {Supabase access token}`만 사용하며 JSON 본문이나
클라이언트가 제시한 사용자 ID는 신뢰하지 않는다.

서버는 publishable key와 사용자 Bearer 토큰으로 `/auth/v1/user`를 조회한 뒤,
그 응답의 `id`만 service role 관리자 삭제 경로에 전달한다. 성공은 본문 없는
204, 토큰 누락·무효는 401, 인스턴스 기준 IP별 분당 3회 초과는 429다.
관리자 삭제 오류는 외부 응답 본문을 노출하지 않는 공통 500으로 바뀐다.

`profiles`와 `user_saved_places`는 `auth.users(id) on delete cascade`라 Auth 사용자
삭제와 함께 제거된다. service role 키는 Vercel 환경변수에만 저장하며 Android
APK나 응답에 포함하지 않는다.

## 6. TourAPI 동기화

### 6.1 기본 장소 동기화

경로: `GET /api/cron/tour-sync`
소스: TourAPI `KorService2/areaBasedList2`

고정 요청값:

```text
MobileOS=ETC
MobileApp=Tteumsae
_type=json
arrange=C
areaCode=32
numOfRows=100
```

한 실행에서 `TOUR_SYNC_MAX_PAGES`만큼 처리하며 기본값 10, 코드상 최대 25다.
각 페이지를 즉시 upsert하고 `sync_state(id='tour_api')`에 다음 페이지를
저장한다. 전체를 마치면 `next_page`를 1로 되돌린다. 오류 시 가능한 경우
`last_error`를 최대 500자로 저장한 뒤, 호출자에게는 공통 500만 반환한다.

성공 응답:

```json
{
  "status": "partial",
  "processedPages": 10,
  "savedPlaces": 1000,
  "totalCount": 2500,
  "nextPage": 11
}
```

`status`는 전체 순회를 마치면 `completed`, 이어서 처리해야 하면 `partial`이다.

### 6.2 상세 보강 동기화

경로: `GET /api/cron/tour-detail-sync`

장소마다 세 요청을 병렬로 실행한다.

| TourAPI 작업 | 용도 |
|---|---|
| `detailIntro2` | 주차·유아·운영시간·휴무일 원문 |
| `detailImage2` | 원본·썸네일 이미지 최대 20건 |
| `detailPetTour2` | 반려동물 동반 정보 존재 여부 |

`TOUR_DETAIL_SYNC_BATCH_SIZE`는 기본 10이고 코드상 최대 10이다. 활성 장소를
`content_id` 오름차순으로 읽고 `sync_state(id='tour_details')`의 페이지를
이어간다. 세 요청이 모두 실패한 장소는 건너뛰며 `failed`를 증가시킨다. 하나라도
성공하면 가능한 데이터만 정규화한다.

```json
{
  "status": "partial",
  "page": 12,
  "processed": 10,
  "updated": 9,
  "failed": 1,
  "nextPage": 13
}
```

상세 데이터는 별도 컬럼 추가 없이 기존 `places.raw` 안에 저장한다.

```json
{
  "_tteumsae": {
    "tags": ["주차 가능", "아이 동반", "반려동물 동반"],
    "imageUrls": ["https://..."],
    "openingHours": "09:00~18:00",
    "closedDays": "매주 월요일",
    "intro": {},
    "pet": {},
    "enrichedAt": "ISO-8601"
  }
}
```

현재 태그 규칙:

- 주차 관련 값이 있고 `없음`, `불가`, `불가능`, `미제공`, `해당없음`이 아니면
  `주차 가능`
- 유아차·아동시설 관련 긍정 값이 있으면 `아이 동반`
- `detailPetTour2` 객체가 있으면 `반려동물 동반`
- 문화시설(`contenttypeid=14`)이면 `실내 활동`

`고령자 동반`, `무장애 시설`은 아직 데이터 연동·정규화가 없다.

### 6.3 Cron 일정

[`backend/vercel.json`](../backend/vercel.json)은 UTC를 사용한다.

| UTC | 한국시간(KST) | 작업 |
|---|---|---|
| 매일 18:20 | 다음 날 03:20 | 기본 장소 동기화 |
| 매일 18:40 | 다음 날 03:40 | 상세 보강 동기화 |

두 작업 모두 최대 실행시간 60초다. 데이터가 많으면 한 번에 전체를 끝내지 않고
`sync_state` 커서를 다음 날 이어서 처리한다.

## 7. Supabase 스키마

마이그레이션 적용 순서:

1. [`001_initial.sql`](../backend/migrations/001_initial.sql)
2. [`002_detail_sync_state.sql`](../backend/migrations/002_detail_sync_state.sql)
3. [`003_user_accounts.sql`](../backend/migrations/003_user_accounts.sql)

### 7.1 `public.places`

| 컬럼 | 타입 | 제약/설명 |
|---|---|---|
| `content_id` | text | PK |
| `source` | text | 기본값 `TOUR_API` |
| `name` | text | NOT NULL |
| `category` | text | NOT NULL |
| `content_type_id` | integer | NOT NULL |
| `area_code` | integer | 기본값 32 |
| `sigungu_code` | integer | nullable |
| `latitude`, `longitude` | double precision | NOT NULL |
| `address`, `image_url`, `tel` | text | nullable |
| `default_stay_minutes` | integer | 5~360 체크 |
| `is_active` | boolean | 기본값 true |
| `source_modified_at` | text | TourAPI 수정시각 원문 |
| `synced_at` | timestamptz | 기본값 now() |
| `raw` | jsonb | TourAPI 원문과 `_tteumsae` 보강 |

인덱스는 `category`, `(latitude, longitude)`, 활성 행의 `is_active`에 있다.

### 7.2 `public.sync_state`

| 컬럼 | 의미 |
|---|---|
| `id` | `tour_api` 또는 `tour_details`, PK |
| `next_page` | 다음 실행에서 읽을 페이지 |
| `total_count` | 마지막 기본 동기화의 TourAPI 총 개수 |
| `last_processed_page` | 마지막 처리 페이지 |
| `last_item_count` | 마지막 페이지 항목 수 |
| `last_error` | 마지막 오류 요약 |
| `last_completed_at` | 전체 순회 완료시각 |
| `updated_at` | 상태 갱신시각 |

`places`, `sync_state`는 RLS가 활성화되어 있고 공개 정책은 마이그레이션에 없다. 서버는
`SUPABASE_SERVICE_ROLE_KEY`로 PostgREST를 호출해 RLS를 우회한다. service role
키를 Android, 브라우저, 로그, 문서에 노출하면 안 된다.

### 7.3 `public.profiles`

선택형 로그인 사용자의 비공개 프로필이다. `user_id`는 `auth.users(id)`를
참조하며 계정 삭제 시 함께 삭제된다. 닉네임은 공백 정리 후 1~40자,
아바타 URL은 최대 2048자다. `age_group`과 `gender`는 nullable 선택값이며
현재 추천 요청이나 점수에는 사용하지 않는다.

authenticated 역할은 본인 행만 조회·추가·수정할 수 있다. `user_id`, 생성시각,
수정시각은 클라이언트가 update할 수 없고 물리 delete 권한도 없다.

### 7.4 `public.user_saved_places`

로그인 사용자의 장소 저장 상태를 `(user_id, place_id)` 복합키로 보관한다.
`is_saved=false` 행도 남기는 tombstone 구조라 여러 기기의 저장 해제를 동기화할
수 있다. `place_id`는 `places(content_id)`를 참조하고 사용자나 장소 삭제 시
연관 행이 함께 삭제된다.

authenticated 역할은 본인 행의 `is_saved`, `saved_at`만 수정할 수 있고 물리
delete는 할 수 없다. [`verify-user-rls.js`](../backend/scripts/verify-user-rls.js)는
테스트 프로젝트에서 임시 사용자 2명을 만들어 본인 CRUD와 교차 사용자 차단을
실제로 확인한다.

## 8. 변경 시 함께 확인할 파일

| 변경 종류 | 백엔드 | Android |
|---|---|---|
| 장소 필드 추가 | `lib/database.js`, 마이그레이션 | `data/TteumsaeApi.kt`, `domain/Models.kt` |
| 장소 지역 필터 변경 | `api/places.js`, `lib/database.js` | `TteumsaeApi.places`, `gangwonRegionCodes`, `SavedPlacesScreen` |
| 카테고리 추가 | `lib/validation.js`, `lib/tour-api.js` | `domain/Models.kt`, 필터 UI |
| 추천 요청 변경 | `lib/validation.js`, `api/recommendations.js` | `data/TteumsaeApi.kt` |
| 추천 응답 변경 | `lib/time-safe.js`, 경로 provider | `toRecommendation()` |
| 기준 경로/corridor 변경 | `api/recommendations.js`, `lib/routing.js`, `lib/kakao-mobility.js` | `RecommendationResult`, `RouteMap` |
| 복수 경유 경로 변경 | `api/route.js`, `parseRouteRequest`, `fetchKakaoRoute` | `TteumsaeApi.route`, `RouteSummary`, `ResultsScreen` |
| 운영시간 규칙 변경 | `lib/time-safe.js`, `lib/tour-api.js` | 카드 상태 문구 |
| Cron 변경 | `vercel.json`, `api/cron/*` | 해당 없음 |

계약 변경에는 반드시 백엔드 Node 테스트와 Android JSON 파서 테스트를 함께
추가한다. 현재 소스의 `sigunguCode` 검증과 route/baseRoute/corridor를 포함한
백엔드 테스트는 `2026-08-27` 기준 41/41 통과했다.

## 9. 알려진 데이터·API 부채

- 공개 API 사용자 인증과 구조화된 요청 추적은 없다. 추천/경로의 현재 IP 제한은
  서버리스 인스턴스별 best-effort라 분산·병렬 남용 방어가 아니다.
- 장소 목록은 총 개수를 제공하지 않는다.
- Android의 지역 카탈로그는 서버 코드 필터를 사용하지만 로컬 찜 사본은 주소
  문자열로 판정해 주소 누락·행정구역 표기 차이에 취약하다.
- `/api/route`는 인증 없이 Kakao Mobility 호출을 만들며 현재 IP 제한만 적용한다.
- 경유지 순서는 사용자가 추가한 순서이며 서버 최적화가 없다.
- Android의 `/api/route` 실패 fallback은 실제 통합 경로가 아닌 단일 후보
  우회값 합산이다.
- API는 이전 클라이언트를 위해 `deadlineMinutes`도 허용한다. 신규 Android는
  `extraTimeMinutes`만 보내므로 계약 변경 시 두 경로의 호환 테스트가 필요하다.
- corridor는 path까지의 평면 근사 거리이며 도로 등시간선이 아니다.
- 도보는 실제 길찾기가 아닌 추정이다.
- 영업시간 파서는 단순 요일·시간 범위만 안전하게 해석한다.
- TourAPI 원문 변경이나 복잡한 휴무 표현은 `UNKNOWN`으로 남는다.
- 카페 분류와 무장애·고령자 태그가 없다.
- 상세 동기화가 모든 장소를 다시 순회하므로 변경분 전용 전략이 없다.
- `vercel.json`의 `/downloads/tteumsae-latest-debug.apk` rewrite는 과거
  `v0.4.0` 파일을 가리키는 레거시 설정이다. 현재 APK 다운로드는 별도
  [`download/`](../download/) 프로젝트에서 관리하므로 수정 또는 제거해야 한다.
