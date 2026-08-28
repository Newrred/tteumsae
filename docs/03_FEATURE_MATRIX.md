# 기능 구현 현황표

기준일: `2026-08-28`
Android 버전: `0.12.4` (`versionCode 25`)

이 표는 현재 `main`의 Gate 2 실제 코드 동작을 기록한다. 자동 검증과 실기기 QA를
구분하며, 향후 아이디어는 [다음 버전 계획](09_NEXT_VERSION_PLAN.md)에만 기록한다.

## 상태 정의

| 상태 | 의미 |
|---|---|
| `코드 검증` | 단위 테스트·컴파일 또는 정적 검사로 연결을 확인함 |
| `실기기 QA 필요` | 코드는 연결됐지만 GPS·지도·외부 앱·알림 등 기기 확인이 필요함 |
| `호환 전용` | 신규 제품 UI에서는 쓰지 않고 이전 계약 회귀 방지를 위해 유지함 |
| `미구현` | 사용자에게 제공하지 않음 |

## 1. 도착 마감 핵심 흐름

| 기능 | 상태 | 현재 동작 | 코드 근거 |
|---|---|---|---|
| 활성 흐름 | `코드 검증` | `HOME → LOCATION → LOADING → RESULTS → DETAIL`; 별도 CONDITIONS 단계 없음 | `ui/TteumsaeApp.kt`, `ui/navigation/AppDestination.kt` |
| 입력 | `코드 검증` | 출발지, 목적지, 절대 도착 마감과 선택 관심 필터를 한 화면에서 입력 | `ui/route/LocationScreen.kt` |
| 도착 마감 검증 | `코드 검증` | 현재부터 15분 이상 24시간 이하; 미선택이 기본이며 시간 선택 확인 시에만 반영 | `domain/route/ArrivalDeadlinePolicy.kt` |
| 상태 소유권 | `코드 검증` | `RouteFlowViewModel + SavedStateHandle`; 위치·마감·필터·선택 ID 복원 | `ui/route/RouteFlowViewModel.kt` |
| 취소 경쟁조건 | `코드 검증` | 로딩 중 뒤로가기/새 검색이 진행 중 Job을 취소해 늦은 응답이 RESULTS를 다시 열지 않음 | `RouteFlowViewModelTest.kt` |

## 2. 추천 결과와 안내

| 기능 | 상태 | 현재 동작 | 한계·확인 |
|---|---|---|---|
| 지도 핀 | `실기기 QA 필요` | 후보 핀에는 `+N분` 추가 이동시간만 표시 | Kakao 지도 밀집·겹침 QA 필요 |
| 한 곳 선택 | `코드 검증` | 선택 없음/한 곳만 허용; 같은 곳 재탭은 해제, 다른 곳은 교체 | 제품 UI만 1곳 제한 |
| 체류 판단 | `코드 검증` | 선택 카드에 `이동 기준 최대 약 N분`과 늦어도 출발할 시각 표시 | 평균 체류시간은 표시하지 않음 |
| 빈 결과 | `코드 검증` | 직행이 빠듯한 경우와 15분 이상 체류 후보가 없는 경우를 구분; 직행 CTA 유지 | 문구·가독성 실기기 QA 필요 |
| 수동 갱신 | `코드 검증` | `현재 교통으로 다시 확인`; 실패 시 마지막 결과 유지 | 앱 복귀 자동 갱신은 미구현 |
| 외부 안내 | `실기기 QA 필요` | 선택 없으면 목적지 직행, 선택 시 한 곳을 경유해 카카오맵 실행 | 카카오맵 설치/웹 fallback QA 필요 |

## 3. 시간 모델과 API

| 기능 | 상태 | 현재 동작 | 코드 근거 |
|---|---|---|---|
| 신규 요청 | `코드 검증` | `timeModel=ARRIVAL_DEADLINE_V1`과 절대 epoch 전송; legacy 시간 필드 미전송 | `data/TteumsaeApi.kt`, `backend/lib/validation.js` |
| 서버 기준시각 | `코드 검증` | 요청 수신시각을 한 번 고정해 남은 시간을 분 단위 내림 | `backend/api/recommendations.js` |
| 안전여유 | `코드 검증` | 서버 고정 10분; 사용자 입력 없음 | `backend/lib/validation.js` |
| 최대 체류 | `코드 검증` | 두 이동구간과 안전여유를 뺀 뒤 5분 단위 내림; 최소 15분 미만 제외 | `backend/lib/time-safe.js` |
| 운영시간 | `코드 검증` | 명확한 영업 종료로 최대 체류를 제한; 해석 불가 UNKNOWN은 유지 | `backend/lib/time-safe.js` |
| legacy 추천 | `호환 전용` | `extraTimeMinutes` 또는 `deadlineMinutes` 요청과 응답 유지 | backend validation/time-safe 테스트 |
| 복수 route | `호환 전용` | 저수준 `/api/route`와 Android wrapper는 경유지 0~5개 유지 | `backend/api/route.js`, `data/TteumsaeApi.kt` |

## 4. 선택형 출발 알림

| 기능 | 상태 | 현재 동작 | 한계·확인 |
|---|---|---|---|
| 노출 | `코드 검증` | 장소를 선택했을 때만 `출발 5분 전에 알려드릴까요?` 토글 표시 | 미선택에는 표시하지 않음 |
| 권한 | `코드 검증` | Android 13+에서 opt-in 시에만 알림 권한 요청; 거부해도 길 안내 가능 | 권한 허용/거부 실기기 QA 필요 |
| 예약 | `실기기 QA 필요` | `setAndAllowWhileIdle`; exact alarm·백그라운드 위치 권한 없음 | 절전 모드에서는 전달 지연 가능 |
| 복원 | `코드 검증` | 재부팅/패키지 교체 후 유효한 여행만 재예약; 도착 마감+2시간 뒤 만료 | 실제 재부팅 QA 필요 |
| 저장 범위 | `코드 검증` | 경로·선택 장소·마감·안내에 필요한 최소 스냅샷만 기기 로컬 저장 | 서버 위치 이력 저장 없음 |
| 수명주기 | `코드 검증` | 새 검색·선택 해제는 이전 알림을 취소하고, 재조회는 선택이 사라지면 취소·남으면 새 출발 마감으로 교체 | `DepartureReminderCoordinator` |
| 백업 제외 | `코드 검증` | 활성 여행·인증 세션이 포함될 수 있는 SharedPreferences를 cloud backup/device transfer에서 제외 | `backup_rules.xml`, `data_extraction_rules.xml` |

## 5. 기존 앱 기능

| 기능 | 상태 | 현재 동작 | 한계·확인 |
|---|---|---|---|
| 홈 Kakao 지도·GPS | `실기기 QA 필요` | 강릉 지도, 위치 권한/설정 안내, 현재 위치 마커 | 키 해시·위치 서비스별 QA 필요 |
| 틈새 발견·저장 | `실기기 QA 필요` | TourAPI 카탈로그, Room 게스트 저장·해제·복원 | 기기 간 동기화 없음; 평균 체류 UI 제거 |
| 선택 로그인 | `실기기 QA 필요` | Google·Kakao Supabase OAuth, 프로필 조회/수정, 탈퇴 | release 서명 OAuth 회귀 필요 |
| 프로필 | `코드 검증` | 닉네임과 선택 연령대·성별 | 추가 필드는 후속 DB/앱 마이그레이션 필요 |
| 정책·지원 | `코드 검증` | 개인정보처리방침, 오픈소스, 문의 메일, 캐시·저장 관리 | 공개 URL 운영 확인 필요 |

## 6. 출시 준비

| 항목 | 상태 | 남은 일 |
|---|---|---|
| Backend unit/check | `코드 검증` | 최종 수치는 [QA 문서](08_QA_AND_KNOWN_ISSUES.md) 기준 |
| Android unit/lint/debug APK | `코드 검증` | 최종 수치는 QA 문서 기준 |
| 실기기 전체 회귀 | `미구현` | GPS, Kakao 지도·딥링크, 알림, OAuth, 자정 넘김 검증 |
| Play release | `미구현` | release signingConfig, 서명 AAB, 스토어 리소스·정책 점검 |
