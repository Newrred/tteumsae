# 틈새 Android MVP

강원도 여행 중 목적지에 늦지 않고 들를 수 있는 장소를 추천하는 Android
네이티브 앱입니다.

## 현재 구현

- Kotlin + Jetpack Compose
- 활성 경로 탐색 모드 (`HOME → LOCATION → CONDITIONS → LOADING → RESULTS → DETAIL`)
- 근처 탐색 모드는 코드에만 남은 비활성 레거시
- 출발지와 목적지 카카오 키워드 검색
- Vercel 운영 백엔드 연결
- TourAPI 기반 강원도 장소 추천
- 활성 경로 탐색은 카카오 자동차 이동시간 적용
- 비활성 근처 탐색 레거시의 도보 분기는 거리 기반 추정시간 사용
- 카카오맵 Android SDK 기반 실제 지도 표시
- 시간 입력 없이 내부 호환값 `extraTimeMinutes=1,440`, `safetyBufferMinutes=15` 사용
- 장소 유형별 기본 머무름을 적용한 레거시 시간 필터와 결과 카드
- 카카오맵 앱 딥링크
- 네트워크 실패 메시지 및 재시도

현재 구현은 신규 도착 마감 제품 플로우가 아닙니다. 다음 목표는 LOCATION에서 목적지와
절대 도착 마감을 받고, 한 곳 선택 후 `이동 기준 최대 체류시간`과 `출발 권장시각`을
표시하는 것입니다. 확정 기준은
`docs/superpowers/specs/2026-08-26-deadline-aware-route-flow-design.md`를 따릅니다.

서버용 TourAPI 키와 카카오 REST API 키는 Android 앱에 포함되지 않습니다.
앱은 공개된 틈새 백엔드 API만 호출합니다.
카카오맵 네이티브 앱 키는 Git에 포함되지 않는 `local.properties`의
`KAKAO_MAP_NATIVE_APP_KEY` 값으로 주입합니다.

## 운영 API

```text
https://tteumsae-backend-one.vercel.app
```

배포 환경별 주소를 분리하려면
`app/build.gradle.kts`의 `BuildConfig.API_BASE_URL`을 빌드 타입별로
설정합니다.

## 실행 환경

- Android Studio Ladybug 이상 권장
- JDK 17
- Android SDK compile 36 / target 35

이 폴더를 Android Studio에서 열고 Gradle Sync 후 `app` 구성을
실행합니다.

## 주요 파일

```text
app/src/main/java/com/tteumsae/app/
├── MainActivity.kt
├── data/
│   ├── TteumsaeApi.kt
│   ├── DataContracts.kt
│   └── SamplePlaces.kt
├── domain/
│   ├── Models.kt
│   └── TimeSafeEngine.kt
└── ui/
    ├── TteumsaeApp.kt
    └── theme/Theme.kt
```

## 다음 출시 작업

- 결과 카드·지도 마커·카메라 동기화 실기기 회귀
- 설정 탭 최종 정보 구조와 디자인
- 온보딩과 위치 권한 영구 거부 안내
- 영업시간·휴무·입장 마감 정규화
- 고령자 동반·무장애 시설 데이터 연동
- 개인정보처리방침 및 위치기반서비스 고지
- 앱 아이콘, 스플래시, 접근성, 다크 모드
- targetSdk 36 전환과 실제 기기 회귀 테스트 — 공개 Play 제출 전 필수
- Play App Signing용 릴리스 서명과 AAB 생성
