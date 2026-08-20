# 기능 구현 현황표

기준일: `2026-08-20`
검토 기준: 현재 통합된 `android/`와, Android가 직접 사용하는 `backend/` 계약
Android 버전: `0.12.3` (`versionCode 24`)

이 표는 디자인 시안이나 과거 APK가 아니라 현재 소스의 실제 동작을 기록한다. 축약 경로 `ui/`, `data/`, `domain/`은 `android/app/src/main/java/com/tteumsae/app/`를 기준으로 한다.

## 상태 정의

| 상태 | 의미 |
|---|---|
| `구현 및 코드 검증` | 사용자 흐름과 예외 처리가 연결돼 있고 자동 테스트 또는 컴파일 근거가 있음. 실기기 회귀는 별도 |
| `구현, QA 필요` | 기능은 연결됐으나 GPS·카카오맵·외부 앱·네트워크 등 실기기 확인이 핵심 |
| `부분 구현/추정` | 화면이나 기본 동작은 있으나 정확도, 복원, 문구 또는 실패 처리에 임시 동작이 있음 |
| `미구현` | 사용자에게 제공할 완성 동작이나 출시 구성이 없음 |

## 1. 활성 탐색 흐름과 공통 상태

| 기능 | 상태 | 현재 실제 동작 | 한계·다음 확인 | 코드 근거 |
|---|---|---|---|---|
| 활성 탐색 흐름 | `구현 및 코드 검증` | `HOME → LOCATION → CONDITIONS → LOADING → RESULTS → DETAIL`. 홈에서 항상 `ON_THE_WAY`와 자동차 탐색을 시작한다 | `TIME`, `NEARBY` 관련 코드가 남아 있으나 활성 UI에서 진입하지 않는 레거시다 | `ui/TteumsaeApp.kt`의 `AppScreen`, `TteumsaeApp` |
| 화면 전환 | `부분 구현/추정` | 단일 Activity/컴포저블이 `AppScreen` enum 값으로 화면을 교체한다 | `NavHost`와 앱 내부 시스템 뒤로가기 백스택이 없고, 좌표·추천·경로는 프로세스 종료 후 복원되지 않는다 | `MainActivity.kt`, `ui/TteumsaeApp.kt` |
| 내부 시간 기준 | `부분 구현/추정` | 홈에서 탐색을 시작할 때 `deadline=1440분`, `safetyBuffer=15분`으로 고정한다. 사용자는 새 흐름에서 시간 화면을 거치지 않는다 | 이는 사실상 넓은 경로 주변 후보 탐색용 상한이다. 사용자의 실제 도착 마감시각을 입력받지 않으므로 `늦지 않음 보장`, `남는 시간`으로 표현하면 안 된다 | `ui/TteumsaeApp.kt`의 `MAX_DEADLINE_MINUTES`, `HomeScreen.onStart`, `SearchCriteria` |
| 브랜드 토큰 | `구현 및 코드 검증` | 기본색 `#E60F33`; 연한 강조색과 내비게이션 그림자에 파생 알파를 사용한다 | 신규 화면에서 임의 빨간색을 추가하지 말고 테마 토큰을 재사용한다 | `ui/theme/Theme.kt`, `ui/TteumsaeApp.kt` |
| 서버 키 보호 | `구현 및 코드 검증` | TourAPI·Kakao REST·Supabase service-role 키는 백엔드에만 있다. Android에는 공개 API 주소와 빌드 시 주입하는 Kakao 네이티브 앱 키만 있다 | CI·release 빌드에서도 네이티브 키를 안전하게 주입해야 한다 | `app/build.gradle.kts`, `data/TteumsaeApi.kt` |

## 2. 홈·내비게이션·GPS

| 기능 | 상태 | 현재 실제 동작 | API·예외 | 코드 근거 |
|---|---|---|---|---|
| 홈 지도 | `구현, QA 필요` | 강릉 중심 Kakao Map, 상단 `목적지 검색`, 우하단 GPS 버튼을 표시한다 | 키 누락 시 플레이스홀더와 설정 안내, SDK 실패 시 `지도 다시 시도` | `ui/TteumsaeApp.kt`의 `HomeScreen`, `KakaoMapSurface` |
| 첫 안내 팝업 | `구현 및 코드 검증` | 서비스 설명과 `오늘 하루 보지 않기`; 체크한 날짜를 SharedPreferences에 저장한다 | 체크하지 않으면 다음 실행에 다시 표시 | `HomeIntroDialog`, `shouldShowHomeIntro`; `ui/HomeIntroTest.kt` |
| 하단 내비게이션 | `구현, QA 필요` | 좌측 `틈새 발견`은 카탈로그/저장 화면, 중앙 돌출 지도 버튼은 홈, 우측은 설정이다. 중앙 지도는 홈에서 브랜드색+그림자, 다른 탭에서 연회색 배경·회색 아이콘/테두리·그림자 없음으로 구분한다 | 작은 화면에서 돌출 버튼과 좌우 탭 터치 영역 QA 필요 | `BottomNavigation`, `BottomNavItem` |
| 홈 GPS 토글 | `구현, QA 필요` | 비활성에서 권한→위치 획득→지도 이동/마커, 활성에서 다시 누르면 해제한다. 활성은 루트 상태가 살아 있는 동안 유지된다 | 최근 60초 위치 우선, 없으면 단일 업데이트 최대 12초. 위치 서비스 꺼짐·실패·영구 거부를 각각 안내 | `ui/CurrentLocation.kt`, `HomeScreen`, `LocationPermissionSettingsDialog` |
| 탐색 시작 | `구현 및 코드 검증` | 검색바를 누르면 현재 GPS 좌표가 있으면 출발지로 전달하고, 없으면 위치 화면에서 자동 요청한다. 동시에 경유 선택을 초기화하고 내부 시간 기준을 24시간/15분으로 설정한다 | 별도 홈 CTA는 없다 | `TteumsaeApp`의 `HomeScreen.onStart` |

## 3. 위치 입력

| 기능 | 상태 | 현재 실제 동작 | API·예외 | 코드 근거 |
|---|---|---|---|---|
| 출발지 | `구현, QA 필요` | 기본은 GPS 현위치. 성공 후 `/api/region` 주소로 바꾸어 한 줄 말줄임 표시한다. 필드를 누르면 지우고 직접 검색할 수 있다 | 역지오코딩 실패 시 `현재 위치` 유지. 현위치 버튼은 활성/비활성 토글이며 최소 48dp | `LocationScreen`, `LocationSearchField`, `GET /api/region` |
| 목적지 | `구현, QA 필요` | 처음 진입 시 자동 포커스. 2자 이상 입력 후 350ms 디바운스하여 최대 5개 검색 결과를 표시한다 | 검색어에 `강원 ` 접두어를 붙이고 최종 좌표도 `/api/region`으로 강원도 여부를 확인한다 | `LocationSearchField`, `data/TteumsaeApi.kt` |
| 진행 검증 | `구현 및 코드 검증` | 출발·목적 좌표가 모두 선택되고 GPS/최종 확인 중이 아닐 때만 `다음` 활성. 성공하면 곧바로 조건 화면으로 이동한다 | 목적지가 강원도 밖이면 토스트 후 유지. 위치·검색 실패는 서버 메시지 또는 네트워크 안내 | `TteumsaeApp`의 `AppScreen.LOCATION`, `LocationScreen` |
| 키보드·작은 화면 | `구현, QA 필요` | 하단 CTA에 `imePadding`, 화면 본문은 `LazyColumn`; 다음 클릭 시 포커스와 키보드를 해제한다 | 작은 화면·큰 글자에서 검색 결과와 고정 CTA가 겹치지 않는지 실기기 QA 필요 | `LocationScreen` |
| 근처 탐색 모드 | `미구현` | `SearchMode.NEARBY`, `ModeSelector`, 도보/차량 분기는 코드에 남아 있지만 현재 홈·위치 UI에서 선택할 수 없다 | 다음 버전에서 다시 연결하거나 레거시 코드를 제거해야 한다 | `domain/Models.kt`, `ui/TteumsaeApp.kt` |

## 4. 관심 조건·시간 의미

| 기능 | 상태 | 현재 실제 동작 | 한계·예외 | 코드 근거 |
|---|---|---|---|---|
| 복수 관심 선택 | `구현 및 코드 검증` | `아무거나`, `식사`, `카페`, `산책·관광`, `실내 활동`, `지금은 음식 제외`를 칩으로 표시한다. 일반 관심은 복수 선택 가능하다 | `아무거나`는 다른 선택을 지우며, 마지막 선택을 끄면 다시 `아무거나`가 된다 | `RecommendationIntent`, `ConditionsScreen`, `toggleRecommendationIntent` |
| 상호 배타 조건 | `구현 및 코드 검증` | `식사`와 `지금은 음식 제외`는 동시에 선택되지 않는다 | 음식 제외는 현재 `RESTAURANT`만 제외하며 `CAFE`는 남는다 | `toggleRecommendationIntent`, `recommendationCategories` |
| 카테고리 매핑 | `부분 구현/추정` | 식사→음식점, 카페→카페, 산책·관광→관광지+레포츠, 실내→문화시설+쇼핑. 여러 관심의 합집합을 서버에 보낸다 | 접근성·동행·날씨 같은 개인화는 없음. TourAPI 카페 분류 공백 가능 | `recommendationIntentFilters` |
| 시간 설정 화면 | `미구현(활성 흐름)` | 15분~6시간 슬라이더와 24시간 직접 입력을 가진 `TimeScreen` 소스는 남아 있다 | `LOCATION` 성공이 바로 `CONDITIONS`로 가므로 사용자가 볼 수 없다. 문서·QA에서 활성 기능으로 간주하지 않는다 | `AppScreen.TIME`, `TimeScreen`, LOCATION 전이 |
| 다음 CTA | `구현 및 코드 검증` | 화면 하단 고정 `다음`; 선택 상태를 카테고리/음식 제외 조건으로 변환하고 로딩으로 이동한다 | 안내 문구는 “1개 이상”이라 쓰지만 `아무거나`가 항상 기본 선택이므로 진행은 항상 가능 | `ConditionsScreen` |

## 5. 추천 요청·기준 경로·로딩

| 기능 | 상태 | 현재 실제 동작 | API·예외 | 코드 근거 |
|---|---|---|---|---|
| 추천 요청 | `구현, QA 필요` | 좌표, `ON_THE_WAY`, 자동차, 선택 카테고리, 내부 `deadline=1440`, `buffer=15`를 전송한다 | `POST /api/recommendations`; 좌표 누락·HTTP 오류는 예외 | `data/TteumsaeApi.kt#recommendations`, `LoadingScreen` 호출부 |
| `baseRoute` | `구현 및 코드 검증` | 서버가 먼저 출발→목적 Kakao Mobility 직행 경로를 계산하고 응답 최상위 `baseRoute`에 시간·거리·통행료·구간·path를 반환한다 | 직행 경로 실패 시 추천 요청 전체가 500 | `backend/api/recommendations.js`, `backend/lib/kakao-mobility.js`, `RecommendationResult` |
| corridor 후보 | `부분 구현/추정` | 직행 path 주변 거리로 DB 후보를 자른다. 반경은 `(deadline-baseRoute.duration)×20m`, 800m~8km이며 응답 `meta.corridorRadiusMeters`로 전달한다 | 현재 24시간 상한 때문에 대부분 최대 8km가 된다. 도로 도달 가능 영역/등시간선이 아니라 path까지의 평면 근사 거리다 | `backend/api/recommendations.js`, `backend/lib/routing.js` |
| 후보별 추천 | `구현 및 코드 검증` | corridor 후보 최대 500개 중 카테고리와 추정 우회가 좋은 최대 20개를 골라, 각 후보 한 곳을 경유한 Kakao 경로를 계산하고 최대 20개를 반환한다 | 일부 실패는 제외, 전부 실패하면 요청 실패. 닫힘으로 확정된 장소 제외, 불명확하면 `UNKNOWN` 유지 | `backend/api/recommendations.js`, `backend/lib/time-safe.js` |
| 로딩 | `부분 구현/추정` | 성공 시 추천, warning, `baseRoute`, corridor 반경을 저장하고 최소 500ms 후 결과로 이동. 실패 시 원인·재시도·조건 복귀 제공 | 로딩 문구가 아직 `남은 시간`과 `도착 전 여유 시간`을 말해 새 경로 주변 탐색 의미와 어긋난다 | `LoadingScreen`, `TteumsaeApp`의 LOADING 분기 |

## 6. 결과·복수 경유지

| 기능 | 상태 | 현재 실제 동작 | 정확도·예외 | 코드 근거 |
|---|---|---|---|---|
| 결과 상단 카드 | `구현, QA 필요` | 뒤로, 총 소요시간, 거리, 통행료, `경유지 N/5 추가됨`, 정보 버튼을 지도 위 카드로 고정한다. 재계산 중에는 시간 자리에 progress를 표시한다 | 총 소요는 `현재 route 주행시간 + 선택 장소 머무름 합계`; 도착 마감까지의 남은 시간이 아니다 | `RouteSummaryCard`, `ResultsScreen` |
| 기준 지도·corridor | `부분 구현/추정` | 최초에는 `baseRoute.path`와 corridor를 그리고 출발/목적 라벨, 후보 핀을 표시한다 | 선택 후 `RouteMap`이 corridor 중심선도 현재 선택 경로로 바꾼다. 서버 후보는 최초 baseRoute corridor로 뽑았으므로 영역은 baseRoute에 고정하는 편이 의미상 맞다 | `RouteMap`, `KakaoMapSurface` |
| 후보 핀 | `구현, QA 필요` | 비선택은 흰 외곽+파랑 카테고리 아이콘, 선택은 브랜드색+우상단 순서 배지. 핀 클릭으로 선택 토글 | 밀집 마커 클러스터링과 TalkBack 설명은 없음 | `createCandidateMarkerBitmap`, `drawCategoryMarkerIcon` |
| 후보 카드 | `구현, QA 필요` | 가로 캐러셀. 대표 이미지, `현재/전체`, 추가 상태, 장소명·상세보기, 첫 구간 거리/시간·카테고리, 평균 머무름, 최대 4개 태그를 표시한다 | 이미지 실패는 기본 이미지. 카드 폭은 화면폭-40dp라 작은 화면과 큰 글자 QA 필요 | `RecommendationCard`, `SavedPlaceImage` |
| 복수 경유지 선택 | `구현 및 코드 검증` | 지도 핀 또는 카드로 최대 5곳 선택/해제. 선택 ID의 추가 순서를 경유 순서로 유지하고 즉시 `/api/route`를 호출한다 | `orderWaypointIdsAlongRoute` 헬퍼는 현재 결과 흐름에서 사용하지 않는다. 자동 최적 순서가 아니라 사용자가 추가한 순서다 | `ResultsScreen`, `TteumsaeApi.route`, `MAX_KAKAO_WAYPOINTS` |
| 실제 경로 재계산 | `구현, QA 필요` | `POST /api/route`가 출발→선택 경유지(0~5)→목적을 Kakao Mobility로 계산해 주행시간·거리·통행료·legs·path를 갱신한다 | 실패 시 토스트 후 단일 후보 우회값을 합친 `ESTIMATE` 경로로 대체한다. 실패 상태를 카드에 지속 표시하지 않는다 | `backend/api/route.js`, `backend/lib/kakao-mobility.js`, `fallbackRouteSummary` |
| 결과 없음 | `구현 및 코드 검증` | `관심 조건 해제하기`, `다른 목적지 검색하기` 제공 | 시간/반경 확대 버튼은 새 결과 UI에 없다. warning은 빈 결과 설명에만 사용된다 | `EmptyRouteResults` |
| 카카오맵 CTA | `구현, QA 필요` | 하단 고정 `이 경로로 카카오맵으로 안내받기`. 선택 0곳이면 직행, 1~5곳이면 선택 순서 경유지와 최종 목적지를 car URL로 전달한다 | 외부 카카오맵/브라우저가 실제 순서와 최종 목적지를 유지하는지 실기기 QA 필요 | `openKakaoMapMultiRoute`, `buildKakaoMapMultiRouteUrl` |

## 7. 새 장소 상세

| 기능 | 상태 | 현재 실제 동작 | 정확도·예외 | 코드 근거 |
|---|---|---|---|---|
| 상단·대표 이미지 | `구현, QA 필요` | 고정 top bar의 뒤로/`장소 상세`/저장 하트와 대표 이미지를 표시한다 | 이미지 없음·실패는 브랜드 기본 이미지 | `DetailScreen`, `SavedPlaceImage` |
| 핵심 지표 | `부분 구현/추정` | 평균 머무름, 현재 위치→장소 시간, 목적지까지 추가 우회시간을 카드로 표시한다 | `현재 위치`라는 문구는 사용자가 직접 고른 출발지에도 그대로 표시된다 | `DetailMetric`, `DetailScreen` |
| 방문 정보 | `부분 구현/추정` | 운영시간·휴무·주차·활동·반려동물, 장소 소개, 주소를 표시한다. 값이 없으면 `정보 확인 필요` | 이용요금은 항상 미확인. 활동/편의는 카테고리·태그 단순 판정이며 TourAPI 원문 정확도에 의존 | `VisitInfo`, `DetailScreen` |
| 저장 | `구현 및 코드 검증` | 하트로 SharedPreferences 저장/해제한다 | 계정 동기화 없음. 저장 직렬화에 운영시간·휴무가 포함되지 않아 재실행 후 저장 사본에서 유실 | `loadSavedPlaces`, `storeSavedPlaces` |
| 상세 CTA | `구현, QA 필요` | 하단에 15분 여유 안내와 `카카오맵에서 경유지로 안내`; 출발→현재 상세 장소→최종 목적지를 연다 | 상세 CTA는 결과에서 선택한 복수 경유지 전체가 아니라 현재 장소 한 곳만 전달한다. 15분은 사용자 도착 마감이 아닌 내부 고정값이라 문구 수정 필요 | `DetailScreen`, `openKakaoMapRoute` |

## 8. 틈새 발견·저장

| 기능 | 상태 | 현재 실제 동작 | 한계·예외 | 코드 근거 |
|---|---|---|---|---|
| 지역 드롭다운 | `구현, QA 필요` | 기본 `강릉`. `강원도 전체`와 강릉·고성·동해·삼척·속초·양구·양양·영월·원주·인제·정선·철원·춘천·태백·평창·홍천·화천·횡성을 선택한다. 변경 시 첫 페이지부터 해당 `sigunguCode`로 다시 요청 | 서버 카탈로그는 코드로 필터하지만 로컬에만 남은 찜 장소는 Android 모델에 코드가 없어 주소 문자열로 지역을 보완 판정 | `gangwonRegionCodes`, `matchesGangwonRegion`, `SavedPlacesScreen`, `TteumsaeApi.places` |
| 카탈로그 | `구현, QA 필요` | 진입 시 선택 지역(기본 강릉)의 `/api/places?page=1&pageSize=100&sigunguCode=...`, 끝 6개 전 같은 지역의 다음 페이지 자동 로드, ID 중복 제거 | 검색·카테고리는 현재까지 받은 선택 지역 페이지에만 적용 | `SavedPlacesScreen`, `TteumsaeApi.places` |
| 검색·필터·개수 | `구현 및 코드 검증` | `틈새 위치 검색`, 전체/찜/7개 카테고리 칩, `스팟 (N)`을 표시한다. 찜과 카테고리는 함께 적용 가능하고 `전체`는 둘 다 해제한다 | N은 서버 전체 수가 아니라 현재 받은 장소+해당 지역의 로컬 찜 중 검색/필터를 통과한 수. tooltip으로 추가 로드를 안내 | `SavedPlacesScreen`, `SavedFilterChip` |
| 카드 | `구현, QA 필요` | 2열 이미지 카드, 155dp 한 줄 말줄임, 평균 머무름, 태그 문자 예산 18과 `+N`, 저장 하트 | 외부 이미지 직접 다운로드, 메모리 캐시만 사용 | `SavedPlaceCard`, `compactTags`, `SavedPlaceImage` |
| 저장·되돌리기 | `구현 및 코드 검증` | 카드 하트 저장 토글, `찜`만 보기, 해제 Snackbar 되돌리기, 저장 우선/이름순 | 로컬 기기에만 저장. `저장 우선순`은 서버 인기순이 아님 | `SavedPlacesScreen` |
| 카탈로그 상세 | `부분 구현/추정` | 이미지·분류·이름·주소·추천 머무름·태그와 카카오맵 장소명 검색 CTA | 추천 상세과 UI/정보 범위가 다르고 좌표 경로 안내가 아니다 | `SavedPlaceDetailScreen`, `openKakaoMap` |

## 9. 설정·정책·지원

| 기능 | 상태 | 현재 실제 동작 | 한계 | 코드 근거 |
|---|---|---|---|---|
| 위치 권한 | `구현, QA 필요` | 권한 상태를 표시하고 앱 설정으로 이동; `ON_RESUME`에 갱신 | 위치 서비스 자체 상태는 별도 표시하지 않음 | `SettingsTabScreen` |
| 카카오맵 상태 | `구현, QA 필요` | 설치 여부 확인, 앱 실행 또는 Play Store/웹 설치 경로 | 기기별 intent 처리 QA 필요 | `isKakaoMapAvailable`, `openKakaoMapHome`, `openKakaoMapInstallPage` |
| 캐시·저장 삭제 | `구현, QA 필요` | 확인 후 이미지 캐시/cacheDir 삭제 또는 저장 목록 전체 삭제 | 지도 SDK 내부 캐시까지 지운다는 보장은 없음 | `SettingsTabScreen`, `clearAppCache` |
| 문의하기 | `구현, QA 필요` | `minjaeimnyda@gmail.com`으로 앱 버전을 포함한 메일 작성 화면 | 출시 전 운영 메일로 교체 | `CONTACT_EMAIL`, `openContactEmail` |
| 개인정보처리방침 | `미구현` | 설정 행은 있으나 URL이 비어 있고 `준비 중` | Play 제출 전 공개 HTTPS 문서 필수 | `PRIVACY_POLICY_URL` |
| 위치기반서비스 약관 | `미구현` | 설정 행은 있으나 URL이 비어 있고 `준비 중` | 법률 검토·공개 문서·동의 정책 필요 | `LOCATION_TERMS_URL` |
| 계정·푸시 | `미구현` | 로그인, 계정 동기화, 푸시가 없음 | 저장 데이터는 로컬 전용 | Android 전체 소스 |

## 10. 릴리스·검증 준비

| 항목 | 상태 | 현재 근거 | 출시 전 작업 |
|---|---|---|---|
| Kotlin 컴파일 | `구현 및 코드 검증` | 최신 통합 소스 `compileDebugKotlin` 성공 | CI로 고정 |
| Debug APK | `구현 및 코드 검증` | 지역/찜 필터와 중앙 지도 비선택 상태를 포함한 최신 통합 소스 `assembleDebug` 성공 (`2026-08-20`) | 새 버전 번호, 실기기 회귀 후 배포 |
| Android 단위 테스트 | `부분 구현/추정` | 테스트 소스는 있으나 `testDebugUnitTest`가 기존 `ClassNotFoundException`으로 실패 | 클래스패스/테스트 출력 경로 해결 전 테스트 통과로 보고 금지 |
| 백엔드 테스트 | `구현 및 코드 검증` | `sigunguCode` 검증을 포함한 Node 테스트 25/25 통과 (`2026-08-20`) | 계약 변경 시 같은 테스트를 다시 실행 |
| Release 서명·AAB | `미구현` | signingConfig/키 전달/CI 없음 | 업로드 키, Play App Signing, `bundleRelease` |
| 정책·스토어 자료 | `미구현` | 정책 URL·런처 아이콘·스토어 제출 체인 미완성 | 정책, 아이콘/스플래시, 데이터 안전, 스크린샷, AAB 준비 |
| 실기기 회귀 | `구현, QA 필요` | 새 결과·상세·복수 경유지 흐름은 코드/빌드 기준 | GPS, Kakao Map SDK, 0~5 경유지, 실패 fallback, 작은 화면·키보드 검증 |

## 11. 변경 시 함께 확인할 파일

| 변경 종류 | 최소 동반 검토 |
|---|---|
| 화면·전이·문구 | `ui/TteumsaeApp.kt`, `04_SCREEN_FLOWS.md`, `08_QA_AND_KNOWN_ISSUES.md` |
| 추천 후보·corridor | `backend/api/recommendations.js`, `backend/lib/routing.js`, `data/TteumsaeApi.kt`, `05_API_AND_DATA.md` |
| 복수 경유 경로 | `backend/api/route.js`, `backend/lib/kakao-mobility.js`, `domain/Models.kt`, `data/TteumsaeApi.kt` |
| 관심 매핑 | `RecommendationIntent`, `toggleRecommendationIntent`, `recommendationIntentFilters`, 관련 Android 테스트 |
| 상세·저장 필드 | `domain/Models.kt`, Android JSON 파서, SharedPreferences 직렬화, TourAPI 동기화 |
| 릴리스 | `app/build.gradle.kts`, 정책 URL, APK/AAB, QA 기록 |
