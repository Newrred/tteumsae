# 다음 버전 실행 계획

기준일: `2026-08-28`

목표는 관광 데이터와 주변 기능을 모두 완성하는 것이 아니라, `도착 시각을 지키며 한
곳을 들를 수 있는가`라는 핵심 가치를 신뢰할 수 있게 검증하는 것이다. 각 Gate는 독립적으로
검증한 뒤 다음 단계로 넘어간다.

## 현재 기준선

- Android 활성 흐름: `HOME → LOCATION → CONDITIONS → LOADING → RESULTS → DETAIL`
- 시간 입력 없음, 내부 `extraTimeMinutes=1,440`, `safetyBufferMinutes=15`
- Android 75/75, Backend 143/143, lint 오류 0, debug APK 빌드 성공
- 운영 장소 1,719곳, 강릉 활성 474곳, 강릉 검수 100곳
- `ARRIVAL_DEADLINE_V1` 요청 검증만 구현, 실제 최대 체류 계산과 Android 연결은 미구현
- Release signingConfig·서명 AAB·실기기 전체 회귀 없음

## Gate 0 — 사실·출시 안전 정리

### 작업

- [x] 현재 구현과 목표 제품 문서 분리
- [x] 운영 백엔드 주소를 `tteumsae-backend-one.vercel.app`으로 정정
- [x] 자동 테스트 기준을 Android 75/75, Backend 143/143으로 갱신
- [x] 로그인 화면과 정책의 기기 간 저장 연속성 약속 제거, 개인정보처리방침 URL 연결
- [x] `targetSdk`를 36으로 올리고 자동 테스트·lint·debug build 회귀
- [x] 프로세스 복원 payload가 없으면 입력 화면 또는 데이터가 있는 이전 화면으로 안전 복귀
- [x] GitHub CI에 Backend test/check와 Android unit/lint 워크플로 구성
- [ ] Release signingConfig·서명 AAB와 release-signed OAuth 실기기 회귀
- [x] Production Branch 직접 배포 대신 Preview smoke 후 명시적 승격 절차 문서화
- [ ] migration 001~005 전체로 빈 DB 복구 리허설

### 통과 조건

- 현재 제공하지 않는 저장 동기화·실시간 추적을 UI와 정책이 약속하지 않는다.
- API 36 대상 빌드와 자동 검증이 통과한다.
- 화면 enum만 복원되고 결과 payload가 사라지는 상태가 사용자에게 노출되지 않는다.

로컬에서는 CI와 같은 Backend·Android 명령까지 통과했다. 최초 GitHub Actions 원격 실행은
커밋 push 후 확인해야 하며, 서명 AAB·실기기 회귀·Preview smoke·Production 승격·빈 DB
복구 리허설은 Gate 0의 외부 환경 검증으로 남아 있다.

## Gate 1 — 최소 데이터·운영 신뢰

### 작업

- [x] Vercel Hobby의 분 단위 Cron 순서에 의존하지 않도록 작업 시간을 UTC 기준 4시간 분리
- [x] 중복 Cron을 막는 90초 DB claim/lease 적용
- [x] Kakao·TourAPI·Supabase fetch에 요청별 timeout과 전체 요청 deadline 적용
- [x] 추천 한 건의 정확 Kakao 경로 후보 상한을 8개로 축소
- [x] 일일 Kakao 호출 예산, 429/5xx와 운영시간 보강률 영속 기록
- [ ] 다음 예약 Cron 실행 후 마지막 성공·요약이 ops에 쌓이는지 확인
- [x] 운영시간 파서가 평일·주말, 계절, 입장 마감을 확실히 구분하지 못하면 `UNKNOWN`
- [x] 지난 축제와 날짜 불완전 축제를 추천에서 제외
- [x] 강릉 알파 핵심 장소 100개 운영시간·주차·입장 정보 검수

### 통과 조건

- 느린 외부 API가 Vercel 60초까지 요청을 붙잡지 않는다.
- 호출량이 Kakao 일일 쿼터를 소리 없이 소진하지 않는다.
- 검증되지 않은 운영시간 때문에 닫힌 장소를 `OPEN`으로 확정하지 않는다.

전체 TourAPI 소개·미디어·반려동물 보강은 이 Gate의 완료 조건이 아니다.

2026-08-28 운영 적용으로 migration 005~006, 7,000/8,000 호출 경계, 검수 overlay
100행과 Production 승격을 확인했다. 후보 배포의 실제 route·recommendations가 성공했고
인증된 ops HTTP는 검수 100/100과 Mobility 예약/성공 10/10을 반환했다. 실제 Production
Cron 수동 실행에서 catalog와 intro 모두 200/completed였고 intro 20건 갱신·실패 0과
두 작업의 마지막 완료 요약 영속화를 확인했다. 다음 예약 트리거 실행 이력만 남아 있다.

## Gate 2 — 도착 마감 1곳 핵심 플로우

### Backend

- [ ] 신규 요청은 절대 `arrivalDeadlineEpochMillis`를 사용
- [ ] 서버 수신시각 기준 남은 시간을 내림 계산
- [ ] 이동시간은 분 올림, 최대 체류시간은 5분 단위 내림
- [ ] 최소 체류 15분, 내부 안전여유 10분 적용
- [ ] `maximumStayMinutes`, `latestDepartureAt`, `detourMinutes` 반환
- [ ] 기존 `extraTimeMinutes` 클라이언트 회귀 유지

### Android

- [ ] `RouteFlowViewModel + SavedStateHandle`로 경로 상태 이동
- [ ] `LOCATION`에서 목적지와 도착 마감을 함께 입력
- [ ] 별도 필수 CONDITIONS 단계를 제거하고 관심 조건을 선택 필터로 전환
- [ ] 추천 핀에 `+N분` 추가 이동시간 표시
- [ ] 결과는 한 곳 선택을 기본 완료 경로로 제공
- [ ] 선택 후 `이동 기준 최대 약 N분`, `H시 M분까지 출발` 표시
- [ ] 고정 로컬 출발 알림을 opt-in으로 제공
- [ ] 카카오맵 실행과 앱 복귀 시 현재 교통 기준 재조회

### 구조 원칙

- `TteumsaeApp.kt` 전면 재작성 금지
- 수정하는 경로 화면부터 `ui/route`로 점진 분리
- 계산 공식은 순수 domain 함수와 테스트에만 존재
- 제품 UI는 1곳만 허용하되 서버 `/api/route` 0~5곳 호환은 유지

### 통과 조건

- 모든 노출 후보가 현재 계산 기준 최소 15분을 확보한다.
- 14분 59초, 느린 요청, 자정 넘김과 마감 경계 테스트가 통과한다.
- 후보 없음과 직행도 빠듯한 상태가 막힌 화면을 만들지 않는다.
- 위치·알림 권한 거부 후에도 외부 내비가 동작한다.

## Gate 3 — 사용자·실경로 검증

### 작업

- [ ] 위치 원본을 분석 서버에 장기 보관하지 않는 최소 이벤트 계측
- [ ] 10~15명 첫 사용성 테스트
- [ ] 30개 실경로 내부 안전성 감사
- [ ] 200개 shadow route에서 오차와 거짓 가능 판정 측정
- [ ] 호텔·축제·렌터카 QR 또는 딥링크 유입 실험

### 핵심 지표

- 5초 내 목적 이해율
- 입력→결과 완료율
- 무결과율
- 첫 장소 선택까지 걸린 시간
- 장소 선택률과 외부 내비 실행률
- 잘못된 `들러도 됨` 판정률
- 사용자의 시간 충분 여부와 정시 도착 자기보고

### 통과 조건

- [제품 범위](01_PRODUCT_AND_SCOPE.md)의 초기 성공 기준을 충족한다.
- 실패 원인이 장소 부족, 데이터 부정확, UI 이해, 경로 계산 중 어디인지 구분된다.

## Gate 4 — 근거가 생긴 뒤 확장

다음은 Gate 3 결과가 필요하다.

- 정확 통합 경로가 가능한 경우의 두 번째 장소
- 지오펜스 도착 감지와 백그라운드 위치 심사
- 저장 장소 클라우드 동기화
- 장소·카테고리별 의미 체류시간 모델
- 전국·iOS·웹 진입
- 사업자 운영·예약·대기 재고
- B2B2C 캠페인과 시간 적합형 쿠폰

## 지금 하지 않는 작업

- 앱 전체 NavHost·DI 전환
- Supabase Realtime
- PostGIS 조기 도입
- 세 곳 이상 일정 계획
- 위치 기반 상시 추적
- 전국 장소 전체 보강
- 프로필 연령대·성별을 근거 없이 추천에 사용
- `AI`, `최적`, `도착 보장` 중심 마케팅

## 바로 다음 실행 단위

1. 도착 마감 설계를 절대시각·1곳 알파 기준으로 개정한다.
2. 기존 광범위 구현 계획을 중지하고 Gate 0과 Gate 2를 독립 계획으로 다시 쪼갠다.
3. Gate 0의 사실성·Play 차단 항목을 먼저 완료한다.
4. Backend 도착 마감 계산을 테스트 우선으로 구현한다.
5. Android 경로 상태 분리와 신규 화면을 이어서 구현한다.
