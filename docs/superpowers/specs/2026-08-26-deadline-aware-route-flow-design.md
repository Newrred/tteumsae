# 도착 마감 기반 경유지 추천 플로우 설계

작성일: `2026-08-26`

상태: 사용자 방향 승인, 구현 전 설계 기준

대상: Android 활성 경로 탐색, 추천 시간 모델, 최대 2곳 경유, 출발 알림, 선택적 도착 감지

## 1. 목적

사용자는 지금 출발하면서 목적지에 반드시 도착해야 하는 시각만 입력한다. 앱은
체류시간을 미리 입력하게 하지 않고 다음을 계산한다.

- 늦지 않고 들를 수 있는 장소
- 기존 경로보다 늘어나는 이동시간
- 선택한 장소에서 머물 수 있는 최대시간
- 가장 늦게 출발해도 되는 시각
- 한 곳을 더 들를 수 있는지 여부

서비스의 책임은 사용자의 체류 계획을 대신 세우는 것이 아니라, 현재 교통 기준으로
가능한 선택 범위와 출발 마감 시각을 명확하게 알려주는 것이다.

## 2. 확정된 제품 원칙

1. 활성 시나리오는 `지금 출발·이동 중`이다. 미래 일정 계획 기능으로 확장하지 않는다.
2. 목적지와 도착 마감 시각은 기존 `LOCATION` 화면에서 함께 입력한다. 시간 전용 화면을 추가하지 않는다.
3. 사용자는 체류시간을 입력하거나 선택하지 않는다.
4. 안전 여유는 시스템 내부 고정값 `10분`이다. 설정 UI를 제공하지 않는다.
5. 장소당 최소 의미 체류시간은 시스템 내부 고정값 `15분`이다.
6. 지도 핀은 장소 유형과 추가 이동시간만 표시한다. 최대 체류시간은 장소 선택 후 표시한다.
7. 기본 완료 경로는 경유지 한 곳이다. 두 번째 경유지는 실제로 가능한 후보가 있을 때만 약한 선택지로 제공한다.
8. 제품상 경유지는 최대 2곳이다. Kakao API와 구버전 호환을 위한 서버의 5곳 지원은 유지할 수 있다.
9. 외부 카카오맵이 실제 주행을 안내한다. 틈새는 이동 상태를 계속 추적한다고 약속하지 않는다.
10. 고정 출발 알림이 기본 안전장치다. 위치 기반 도착 감지와 재계산은 선택 기능이다.
11. 위치·알림 권한을 거부해도 장소 추천과 외부 내비 실행은 정상 동작해야 한다.

## 3. 구현 범위 분리

전체 경험은 하나지만 배포와 검증은 두 하위 프로젝트로 분리한다.

### 3.1 1차 - 핵심 도착 마감 경로

- `LOCATION`에 도착 마감 시각 입력 추가
- 실제 남은 전체 시간을 사용하는 추천 계약
- 최소 15분 기준 후보 필터와 최대 체류시간 계산
- 기본 1곳, 선택적으로 최대 2곳인 결과 흐름
- 고정 출발 알림과 외부 내비 연결
- 프로세스 종료 후에도 알림에 필요한 확정 경로 저장

이 범위만 배포돼도 독립적인 상용 흐름이 된다. 백그라운드 위치 권한을 요구하지 않는다.

### 3.2 2차 - 선택적 도착 감지

- 경유지 지오펜스 등록
- 도착 추정 알림과 `아직 도착 전이에요` 정정 동작
- 도착 감지 또는 앱 재진입 시 현재 시각·교통 기준 재계산
- 감지 지연, 네트워크 실패, 권한 거부 시 고정 알림 유지
- 백그라운드 위치 고지, 개인정보처리방침, Google Play 권한 심사 자료

2차가 지연되거나 비활성화되어도 1차 동작은 달라지지 않는다.

구현 계획도 두 문서로 분리한다. 첫 계획은 1차 핵심 경로만 구현하고, 두 번째 계획은
1차가 검증된 뒤 지오펜스·권한·정책 준비를 별도 완료한다.

## 4. 사용자 흐름

활성 화면 전이는 유지한다.

```text
HOME → LOCATION → CONDITIONS → LOADING → RESULTS → DETAIL
```

### 4.1 LOCATION

기존 출발지·목적지 카드 아래에 `몇 시까지 도착해야 하나요?` 행을 둔다.

- 사용자는 시·분을 선택한다.
- 별도 화면으로 이동하지 않고 Material 시간 선택 다이얼로그를 연다.
- 선택 시각은 현재보다 이후인 가장 가까운 시각으로 해석한다.
- 자정을 넘긴 시각은 다음 날로 해석한다.
- 시각 계산 기준 시간대는 `Asia/Seoul`이다.
- 허용 범위는 지금부터 `15분 이상 24시간 이하`다.
- 출발지, 목적지, 도착 마감이 모두 유효할 때만 `다음`을 활성화한다.
- 홈에서 새 탐색을 시작하면 이전 도착 마감은 지운다. 임의 기본값은 적용하지 않는다.

### 4.2 CONDITIONS와 LOADING

관심 조건 선택 흐름은 유지한다. 추천 요청 직전에 절대 도착 시각에서 남은 분을 다시
계산해 검색 중 경과한 시간을 반영한다.

도착 마감이 이미 지났거나 남은 시간이 15분 미만이면 API를 호출하지 않고
`도착 시간을 다시 설정해 주세요`를 안내한다.

### 4.3 RESULTS - 아직 선택하지 않은 상태

- 서버가 시간 조건을 만족한 후보만 지도와 카드에 제공한다.
- 핀에는 `+8분`처럼 직행 대비 추가 이동시간을 표시한다.
- 카드에는 `추가 이동`, `이동 기준 최대 체류시간`, 장소 유형을 표시한다.
- `평균 머무름`과 미리 적용된 체류시간 표현은 제거한다.
- 카드 주요 행동은 `이곳 들르기`, 보조 행동은 `상세보기`다.

후보가 없지만 직행은 가능한 경우:

```text
지금은 경유지 없이 바로 가는 게 안전해요.
[목적지로 바로 출발하기]
```

직행도 도착 마감을 넘기는 경우:

```text
지금 출발해도 도착 시간이 빠듯해요.
현재 교통 상황을 확인하고 바로 출발해 주세요.
[목적지로 바로 출발하기]
```

### 4.4 첫 번째 경유지 선택

장소를 선택하면 다음을 표시한다.

- `기존 경로보다 N분 더 이동해요`
- `이동 기준 최대 N분 머물 수 있어요`
- `오후 H시 M분까지 출발하면 돼요`
- 주요 버튼 `이 경로로 안내받기`
- 보조 선택 `출발 5분 전 알림 받기`

이 상태가 기본 완료 흐름이다. 사용자가 추가 선택을 하지 않아도 모든 핵심 가치를 얻는다.
알림은 첫 사용 시 꺼진 상태이며 사용자가 켤 때만 알림 권한을 요청한다. 한 번 켠 사용자의
선호는 다음 경로에 적용하되 경로 확정 화면에서 언제든 끌 수 있다. 권한 요청 결과와 관계없이
외부 내비는 즉시 열 수 있어야 한다.

두 번째 경유지가 실제 통합 경로 기준으로 가능한 후보가 있을 때만 다음 보조 행동을 노출한다.

```text
한 곳을 더 들를 여유가 있어요.
[경유지 하나 더 둘러보기]
```

### 4.5 두 번째 경유지 탐색

첫 장소가 선택되면 우회시간이 짧은 미선택 후보 최대 6곳을 기존 `POST /api/route`로
비동기 검증한다. 동시 요청은 2개로 제한하고 첫 장소, 도착 마감, 후보 목록이 바뀌면
이전 검증을 취소한다.

- 정확한 통합 경로가 최소 체류 `15분 × 2곳`과 안전 여유 10분을 만족한 후보만 제공한다.
- 검증된 경로 결과는 첫 장소 ID와 두 번째 후보 ID 조합으로 화면 생명주기 동안 캐시한다.
- 가능한 후보가 하나도 없으면 보조 행동을 표시하지 않는다.
- 두 번째 탐색 핀에는 첫 장소만 들르는 경로 대비 추가 이동시간을 표시한다.
- 두 번째 장소를 선택하면 탐색 모드를 종료한다.

첫 장소를 바꾸면 두 번째 장소와 검증 캐시를 함께 지운다. 두 번째 장소만 제거하면 첫
장소 선택은 유지한다.

### 4.6 두 곳 선택 완료

두 장소의 최대시간을 각각 동시에 사용할 수 있는 것처럼 표시하지 않는다.

```text
두 장소에서 합쳐 최대 40분 머물 수 있어요.
첫 장소에서 최대 25분 머물면 다음 장소에서도 15분을 확보할 수 있어요.
```

주요 버튼은 `이 경로로 안내받기`, 보조 행동은 `두 번째 경유지 빼기`다. 제품상 세 번째
경유지 추가 행동은 제공하지 않는다. 알림을 켠 경우 두 경유지의 출발 마감 5분 전에 각각
알림을 예약한다.

## 5. 시간 모델

### 5.1 상수

```text
SAFETY_BUFFER_MINUTES = 10
MINIMUM_STAY_MINUTES = 15
DEPARTURE_REMINDER_LEAD_MINUTES = 5
MAX_PRODUCT_WAYPOINTS = 2
SECOND_STOP_PROBE_LIMIT = 6
SECOND_STOP_PROBE_CONCURRENCY = 2
```

상수는 도메인 모듈 한 곳에서 관리한다. UI와 API 호출부에 숫자를 중복 작성하지 않는다.

### 5.2 요청 시간

```text
deadlineMinutes = ceil((arrivalDeadlineEpochMillis - nowEpochMillis) / 60,000)
```

`deadlineMinutes`는 직행시간을 포함한 전체 남은 시간이다. 기존 Android가 사용하던
`extraTimeMinutes=1,440`과 의미가 다르다.

### 5.3 한 곳 후보

```text
candidateDrivingMinutes = firstLegMinutes + secondLegMinutes
detourMinutes = candidateDrivingMinutes - baseRouteMinutes
maximumStayMinutes = deadlineMinutes - candidateDrivingMinutes - SAFETY_BUFFER_MINUTES
eligible = maximumStayMinutes >= MINIMUM_STAY_MINUTES
latestDepartureAt = arrivalDeadlineAt - secondLegMinutes - SAFETY_BUFFER_MINUTES
```

영업시간을 구조적으로 해석할 수 있으면 예상 도착부터 최소 15분 동안 영업하는 장소만
남기고, 닫는 시각이 더 빠르면 최대 체류시간을 닫는 시각까지로 줄인다. 영업시간을
해석할 수 없으면 이동 기준 최대시간을 사용하고 `영업시간 확인 필요` 상태를 유지한다.

### 5.4 두 곳 경로

두 경유지 통합 경로의 legs를 `[L0, L1, L2]`로 정의한다.

```text
combinedStayMinutes = deadlineMinutes - (L0 + L1 + L2) - SAFETY_BUFFER_MINUTES
eligible = combinedStayMinutes >= MINIMUM_STAY_MINUTES × 2

firstLatestDepartureAt = arrivalDeadlineAt
  - SAFETY_BUFFER_MINUTES
  - L1 - L2
  - MINIMUM_STAY_MINUTES

secondLatestDepartureAt = arrivalDeadlineAt
  - SAFETY_BUFFER_MINUTES
  - L2

firstMaximumStayMinutes = combinedStayMinutes - MINIMUM_STAY_MINUTES
```

선택 완료 화면에는 `combinedStayMinutes`와 `firstMaximumStayMinutes`만 설명한다. 두
장소별 최대시간을 나란히 표시하지 않는다.

### 5.5 교통 변화

초기 계산값은 현재 교통 스냅샷이다. 외부 내비가 경로를 다시 계산할 수 있으므로 틈새는
`도착 보장`이나 `실시간 지속 추적`을 표현하지 않는다.

- 앱이 다시 포그라운드가 되면 현재 시각으로 남은 시간을 다시 계산한다.
- 2차 위치 기능에서는 도착 감지 시 남은 경로를 다시 조회한다.
- 재조회 실패 시 마지막 계산값과 고정 알림을 유지하고 `현재 교통을 다시 확인하지 못했어요`를 표시한다.

## 6. API 계약과 호환

### 6.1 신규 Android 요청

신규 Android는 기존 `POST /api/recommendations`에 다음 계약을 사용한다.

```json
{
  "mode": "ON_THE_WAY",
  "start": { "latitude": 37.1, "longitude": 128.1 },
  "destination": { "latitude": 37.5, "longitude": 127.1 },
  "deadlineMinutes": 120,
  "safetyBufferMinutes": 10,
  "timeModel": "ARRIVAL_DEADLINE_V1",
  "transport": "CAR",
  "categories": ["CAFE"]
}
```

`deadlineMinutes`와 `extraTimeMinutes` 중 하나만 허용하는 현재 규칙은 유지한다.

### 6.2 서버 호환 분기

- `timeModel=ARRIVAL_DEADLINE_V1`: 최소 15분과 최대 체류시간 모델을 사용한다.
- `timeModel` 없음: 기존 `default_stay_minutes`와 `extraTimeMinutes` 동작을 유지한다.
- `POST /api/route`의 최대 5개 계약은 구버전·Kakao 호환을 위해 유지한다.
- 신규 Android UI와 도메인 검증만 최대 2곳으로 제한한다.

### 6.3 신규 응답 필드

신규 시간 모델의 추천 항목에 다음 필드를 추가한다.

```json
{
  "minimumStayMinutes": 15,
  "maximumStayMinutes": 52,
  "totalMinutes": 73,
  "marginMinutes": 47,
  "route": {
    "firstLegMinutes": 20,
    "secondLegMinutes": 38,
    "detourMinutes": 8
  }
}
```

`maximumStayMinutes`는 안전 여유를 이미 제외한 값이다. 신규 Android는 기존
`stayMinutes`를 시간 적합 판정과 결과 표시에서 사용하지 않는다. 신규 모델의
`totalMinutes`는 `차량 이동시간 + minimumStayMinutes`, `marginMinutes`는
`deadlineMinutes - totalMinutes`로 정의한다.

## 7. Android 구조

현재 `TteumsaeApp.kt`는 5,091줄이며 화면, 상태, 지도, 딥링크를 한 파일에서 관리한다.
이번 기능과 직접 관련된 부분만 `ui/route`로 분리한다. 앱 전체 Navigation·DI 전환은 하지 않는다.

### 7.1 도메인과 상태

| 파일 | 책임 |
|---|---|
| `domain/RouteFlowModels.kt` | 절대 도착 마감, 경유 선택, 화면용 시간 결과 모델 |
| `domain/TripTiming.kt` | 상수, 한 곳·두 곳 공식, 출발 마감·알림 시각 계산 |
| `ui/route/RouteFlowUiState.kt` | 후보, 선택 경유지, 검증 중 상태, 가능한 두 번째 후보 캐시 |
| `ui/route/RouteFlowViewModel.kt` | 추천 호출, 경유지 선택, 최대 6개 2차 후보 검증, 취소·오류 처리 |

`RouteFlowViewModel`만 비동기 경로 계산과 선택 규칙을 소유한다. Composable은 계산 공식을
직접 구현하지 않고 이벤트를 전달하고 `RouteFlowUiState`를 렌더링한다.

### 7.2 화면 분리

| 파일 | 책임 |
|---|---|
| `ui/route/LocationScreen.kt` | 출발지·목적지·도착 마감 입력과 검증 |
| `ui/route/ResultsScreen.kt` | 0곳·1곳·2곳 상태, 점진적 두 번째 경유지 탐색 |
| `ui/route/RouteResultComponents.kt` | 후보 카드, 요약 카드, 빈 결과, 출발 알림 선택 UI |
| `ui/route/RouteMap.kt` | 후보 핀, 추가 이동시간 라벨, 선택 순서 표시 |

기존 `TteumsaeApp.kt`는 화면 전환, 상위 탭과 공통 저장 장소 기능만 유지한다.

### 7.3 확정 경로와 알림

| 파일 | 책임 |
|---|---|
| `reminder/ActiveTrip.kt` | 도착 마감, 경유지 1~2곳, 출발 마감, 다음 경로 URL |
| `reminder/ActiveTripStore.kt` | SharedPreferences 기반 확정 경로 저장·만료·삭제 |
| `reminder/DepartureReminderScheduler.kt` | 스케줄러 인터페이스와 취소 계약 |
| `reminder/AlarmManagerDepartureReminderScheduler.kt` | 출발 마감 5분 전 로컬 알림 예약 |
| `reminder/DepartureReminderReceiver.kt` | 알림 표시와 다음 외부 내비 PendingIntent 실행 |
| `reminder/ReminderNotifications.kt` | 채널, 제목, 본문, 알림 ID 생성 |
| `reminder/ReminderRescheduleReceiver.kt` | 기기 재부팅·시각·시간대 변경 후 유효한 알림 복원 |

1차 스케줄러는 백그라운드 위치를 사용하지 않는다. OS 제한으로 정확한 초 단위 전달을
약속하지 않으며, 사용자 문구는 계산된 목표 시각과 `출발 시간이 가까워졌어요`를 사용한다.

확정 경로는 도착 마감 2시간 뒤 자동 만료한다. 사용자가 새 경로를 확정하거나 알림을
끄면 기존 알림과 저장 경로를 모두 교체·삭제한다.

기기 재부팅이나 시스템 시각·시간대 변경 후에는 저장된 절대 도착 마감을 기준으로 남은
출발 알림을 다시 계산한다. 이미 지난 알림은 다시 울리지 않고 다음 유효 알림만 복원한다.

### 7.4 선택적 도착 감지

2차에서 다음 파일을 추가한다.

| 파일 | 책임 |
|---|---|
| `arrival/ArrivalGeofenceManager.kt` | 반경 150m, 2분 체류 기준 지오펜스 등록·해제 |
| `arrival/ArrivalGeofenceReceiver.kt` | 도착 이벤트 수신과 재계산 작업 요청 |
| `arrival/ArrivalRefreshWorker.kt` | 현재 좌표·남은 경로 조회, 알림 재예약, 실패 fallback |
| `arrival/ArrivalNotifications.kt` | 도착 추정, 정정, 권한 안내 알림 |

도착 감지는 초기 온보딩에서 요청하지 않는다. 경로 확정 후 사용자가 `도착 알림 받기`를
선택한 시점에 알림 권한과 위치 사용 이유를 설명한다. 백그라운드 위치는 별도 명시적
동의 후 요청한다.

## 8. 상태와 데이터 흐름

```text
사용자 시간 선택
  → arrivalDeadlineEpochMillis 저장
  → 추천 직전 deadlineMinutes 재계산
  → POST /api/recommendations (ARRIVAL_DEADLINE_V1)
  → 후보의 maximumStayMinutes 표시
  → 첫 경유지 선택
  → 상위 6개 후보 POST /api/route 검증
  → 가능한 경우만 두 번째 탐색 CTA 노출
  → 1곳 또는 2곳 경로 확정
  → TripTiming으로 출발 마감·알림 시각 계산
  → ActiveTrip 저장 및 고정 알림 예약
  → 외부 카카오맵 실행
```

2차 기능이 켜진 경우에만 다음 흐름이 추가된다.

```text
ActiveTrip 저장
  → 경유지 지오펜스 등록
  → 도착 추정 이벤트
  → 현재 시각·남은 경로 재조회
  → 남은 체류 가능 시간과 알림 갱신
  → 도착 추정 알림
```

## 9. 오류와 대체 동작

| 상황 | 사용자 동작 |
|---|---|
| 도착 마감이 지남 | LOCATION 유지, 시간 재선택 안내 |
| 직행부터 마감 초과 | 즉시 출발 경고와 외부 내비 CTA |
| 추천 후보 없음 | 직행 CTA 제공 |
| 첫 경유지 경로 재계산 실패 | 선택하지 않고 기존 후보 화면 유지 |
| 두 번째 후보 검증 일부 실패 | 성공한 후보만 사용, 전부 실패하면 CTA 숨김 |
| 두 번째 후보가 정확한 경로에서 부적합 | 후보 목록에서 제외 |
| 알림 권한 거부 | 경로 안내는 계속, 알림만 비활성 |
| 백그라운드 위치 권한 거부 | 고정 알림 유지, 도착 감지 생략 |
| 지오펜스 지연·오탐 | 기존 알림 유지, `아직 도착 전이에요`로 정정 |
| 도착 후 경로 재조회 실패 | 마지막 출발 마감 유지, 교통 재확인 실패 안내 |
| 앱 프로세스 종료 | ActiveTripStore에서 알림에 필요한 정보 복원 |
| 기기 재부팅·시각 변경 | 만료되지 않은 다음 알림만 다시 예약 |

## 10. 권한과 개인정보

### 10.1 1차

- Android 13 이상에서는 사용자가 출발 알림을 선택할 때 알림 권한을 요청한다.
- 알림 권한은 추천이나 외부 내비 실행의 전제 조건이 아니다.
- 정확 알람 특별 권한에 의존하지 않는다.

### 10.2 2차

- 지오펜스는 Android 10 이상에서 백그라운드 위치 권한이 필요하다.
- Google Play의 백그라운드 위치 승인, 앱 내 주요 고지, 개인정보처리방침, 시연 영상 준비 후 활성화한다.
- 위치는 활성 경로의 도착 감지와 남은 경로 재계산에만 사용한다.
- 활성 경로 만료 후 지오펜스와 로컬 위치 관련 상태를 제거한다.
- 광고, 분석, 사용자 프로필 생성에 위치를 사용하지 않는다.

관련 공식 기준:

- <https://developer.android.com/develop/sensors-and-location/location/geofencing>
- <https://support.google.com/googleplay/android-developer/answer/9799150>
- <https://developer.android.com/develop/ui/compose/notifications/notification-permission>

## 11. 테스트 전략

### 11.1 Android 단위 테스트

- 같은 날과 자정 넘김 도착 마감 변환
- 분 올림과 15분·24시간 경계
- 한 곳 최대 체류, 출발 마감, 알림 시각
- 두 곳 합산 체류, 첫 장소 최대 체류, 두 출발 마감
- 최소 체류 15분 경계값 29분 실패·30분 성공
- 첫 경유지 변경 시 두 번째 선택·캐시 초기화
- 최대 6개, 동시 2개 검증과 취소
- 권한 거부 시 경로 실행 유지
- ActiveTrip 저장·복원·만료·교체
- 재부팅·시각 변경 후 지난 알림 제외와 다음 알림 복원

### 11.2 백엔드 테스트

- `ARRIVAL_DEADLINE_V1` 검증과 알 수 없는 모델 거부
- 최대 체류 14분 제외·15분 포함
- 안전 여유 10분 반영
- 영업 종료 전 최소 15분 확보 여부
- 기존 `extraTimeMinutes` 응답 회귀
- 기존 `POST /api/route` 0·1·5개 회귀

### 11.3 Compose와 실기기 QA

- LOCATION 작은 화면·키보드·시간 다이얼로그
- 후보 핀과 카드의 추가 이동시간 일치
- 0곳→1곳→2곳과 역방향 제거
- 두 번째 후보 탐색 중 로딩·부분 실패·전부 실패
- 카카오맵에 0·1·2곳과 최종 목적지 전달
- 알림 허용·거부·이미 허용 상태
- 앱 종료 후 고정 알림과 다음 경로 CTA
- 2차 활성 시 실제 지오펜스 도착·지연·오탐·권한 설정 변경

## 12. 배포 순서

1. 서버에 `ARRIVAL_DEADLINE_V1` 호환 분기와 테스트를 배포한다.
2. 운영 API에 신규 요청을 보내 최대 체류 필드와 기존 요청 회귀를 스모크 테스트한다.
3. Android 1차 기능을 배포한다.
4. 실제 기기에서 도착 마감, 0·1·2곳, 외부 내비, 고정 알림을 검증한다.
5. 개인정보처리방침과 Google Play 백그라운드 위치 심사 준비를 완료한다.
6. Android 2차 도착 감지를 기능 플래그로 배포한다.
7. 권한 승인과 실기기 검증 후 기본 사용자에게 노출한다.

서버를 먼저 배포해도 구버전 Android 동작은 유지돼야 한다.

## 13. 비목표

- 사용자가 장소별 체류시간을 직접 배분하는 기능
- 세 곳 이상의 여행 일정 계획
- 외부 내비와 동일한 턴바이턴 안내
- 이동 중 상시 위치 추적
- 사용자별 안전 여유·최소 체류 설정
- 서버 계정 기반 여행 기록 동기화
- 앱 전체 Navigation Compose·DI 전환

## 14. 완료 기준

다음 조건을 모두 만족하면 1차 핵심 흐름을 완료로 본다.

- 사용자가 목적지와 도착 마감만 입력하고 후보를 볼 수 있다.
- 모든 후보가 최소 15분과 안전 여유 10분을 만족한다.
- 핀에는 추가 이동시간, 선택 후에는 최대 체류시간과 출발 마감이 표시된다.
- 한 곳 선택만으로 외부 내비를 시작할 수 있다.
- 정확히 가능한 두 번째 후보가 있을 때만 보조 CTA가 나타난다.
- 두 곳 선택 시 합산 체류시간과 첫 장소 최대시간을 오해 없이 표시한다.
- 위치 권한 없이 고정 출발 알림과 외부 내비가 동작한다.
- 권한 거부, 네트워크 실패, 후보 없음이 막힌 화면을 만들지 않는다.
- 신규·기존 API 계약 테스트와 Android 단위 테스트가 통과한다.
