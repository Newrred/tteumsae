# QA와 알려진 문제

기준일: `2026-08-28`
대상 흐름: `HOME → LOCATION → LOADING → RESULTS → DETAIL`

## 1. 자동 검증 기준선

| 대상 | 상태 | 주의 |
|---|---|---|
| Android `testDebugUnitTest` | 116/116 성공 | 도착 마감 경계, V1 직렬화/파싱, ViewModel 복원·취소, 단일 선택, 만료 알림·백업·오류 계약 포함 |
| Android `lintDebug` | 오류 0 | 경고 35개; deprecated 아이콘·API 등은 후속 정리 |
| Android `assembleDebug` | 성공 | APK SHA-256 `12E3D987C82417D928A0B7D07E01A9FCA837E5D5D614F8C14CE1F4B2CCC82B93` |
| Backend `pnpm test` | 154/154 성공 | V1·legacy·운영시간·route 호환, 보수적 우회시간과 작업공간 인수인계 포함 |
| Backend `pnpm check` | 84개 파일 통과 | 구조·보안·마이그레이션 검사 포함 |
| 실기기 회귀 | 미실행 | 지도, GPS, 카카오맵, 알림, OAuth는 기기 확인 필요 |

## 2. 로컬 자동 검증

### Android

```powershell
cd android
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

### Backend

```powershell
cd backend
pnpm install --frozen-lockfile
pnpm test
pnpm check
```

Node.js 24.x를 사용한다. 실제 외부 키나 외부 응답 전문을 테스트 로그에 남기지 않는다.

## 3. Gate 2 Android 실기기 체크리스트

### LOCATION

- [ ] 출발지와 목적지를 검색하고 현위치 권한 거부 후에도 직접 입력할 수 있다.
- [ ] 도착 마감은 처음에 미선택이고 선택기 취소 시 값이 생기지 않는다.
- [ ] 현재부터 15분 미만과 24시간 초과를 거부한다.
- [ ] 자정 이후 시각을 다음 날 마감으로 해석한다.
- [ ] 관심 필터를 선택하지 않아도 진행할 수 있다.
- [ ] 별도 CONDITIONS 화면 없이 바로 LOADING으로 이동한다.
- [ ] 키보드, 320dp 폭, 큰 글자와 gesture/3-button navigation에서 CTA가 잘리지 않는다.

### LOADING·RESULTS

- [ ] 로딩 중 뒤로가면 LOCATION으로 돌아가고 늦은 응답이 RESULTS를 다시 열지 않는다.
- [ ] 지도 핀은 `+N분`만 표시하고 평균 체류나 선택 순번을 표시하지 않는다.
- [ ] 같은 후보 재탭은 해제하고 다른 후보 탭은 한 곳 선택을 교체한다.
- [ ] 선택 카드에 최대 체류와 늦어도 출발할 시각이 서버 값과 일치한다.
- [ ] 미선택 상태에서도 목적지 직행 CTA가 동작한다.
- [ ] 후보 없음과 직행 자체가 빠듯한 상태에서 막힌 화면이 생기지 않는다.
- [ ] 수동 재확인 성공 시 결과가 갱신되고 실패 시 마지막 결과가 유지된다.
- [ ] 앱 강제 종료/복원 시 payload 없는 RESULTS가 LOCATION으로 안전 복귀한다.

### 카카오맵·알림

- [ ] 선택 없음은 목적지 직행, 선택 한 곳은 해당 장소를 경유해 카카오맵이 열린다.
- [ ] 카카오맵 미설치 시 웹 또는 설치 안내 fallback이 동작한다.
- [ ] 장소를 선택했을 때만 출발 5분 전 알림 토글이 보인다.
- [ ] Android 13+에서 토글을 켤 때만 알림 권한을 요청한다.
- [ ] 권한 거부 후에도 카카오맵 안내가 동작한다.
- [ ] 알림이 예상 시각에 오고 탭하면 저장된 카카오 경로 URL이 열린다.
- [ ] 새 검색·선택 해제는 기존 알림을 취소한다.
- [ ] 재조회에서 선택이 사라지면 알림 취소, 남으면 새 출발 마감으로 재예약된다.
- [ ] 재부팅/앱 업데이트 뒤 유효한 알림만 재예약된다.
- [ ] 절전 모드에서 inexact 알림이 지연될 수 있음을 제품 문구가 과장하지 않는다.

### 기존 기능 회귀

- [ ] 홈 Kakao 지도와 GPS 권한·설정 분기가 동작한다.
- [ ] TourAPI 카탈로그, Room 저장·해제·되돌리기와 상세가 동작한다.
- [ ] 저장 목록·상세 어디에도 평균 체류시간이 사용자 지표로 표시되지 않는다.
- [ ] Google·Kakao 로그인, 프로필 수정과 탈퇴가 release 서명 기기에서 동작한다.
- [ ] 설정의 정책 URL, 문의 메일, 캐시·저장 관리가 동작한다.

## 4. Backend 계약 체크리스트

- [ ] V1의 정확히 15분·24시간 경계는 허용하고 바로 바깥은 400이다.
- [ ] V1과 legacy 시간 필드를 섞으면 400이다.
- [ ] 서버 수신시각이 계산 전체에서 고정된다.
- [ ] 최대 체류는 5분 단위 내림이며 15분 미만 후보를 제외한다.
- [ ] 영업 종료가 명확하면 최대 체류를 제한하고 UNKNOWN은 유지한다.
- [ ] V1 meta와 추천별 필수 필드가 모두 반환된다.
- [ ] legacy `extraTimeMinutes` 요청이 회귀하지 않는다.
- [ ] `POST /api/route`의 경유지 0~5개 호환이 유지된다.
- [ ] Preview에서 health와 V1 recommendations 계약을 확인한 뒤 Production을 승격한다.

## 5. 알려진 문제와 비범위

### P0 — 출시 전 필수

- release signingConfig와 서명 AAB가 없다.
- API 36 실제 기기 전체 회귀가 없다.
- Google·Kakao OAuth release 서명 회귀가 없다.
- Gate 2 Preview 배포는 Vercel에서 `BLOCKED/UNKNOWN` 상태로 멈췄고, 기존 Ready
  Preview는 Supabase·Kakao Preview 환경변수가 없어 V1 유효 요청 smoke를 할 수 없다.
  Production은 변경하지 않았다.

### P1 — 핵심 UX 검증

- 교통 데이터는 요청 시점 스냅샷이며 자동 실시간 추적이 아니다.
- 카카오맵이 경로를 다시 계산하므로 앱의 최대 체류와 실제 안내가 달라질 수 있다.
- 위치 기반 경유지 도착 감지와 geofence는 이번 Gate에 포함하지 않는다.
- 알림은 exact가 아니어서 절전 정책에 따라 늦을 수 있다.
- 운영시간 UNKNOWN 장소는 사용자가 직접 확인해야 한다.
- 앱 복귀 자동 재계산은 없고 현재는 명시적 수동 재확인만 제공한다.

### P2 — 구조·운영

- `TteumsaeApp.kt`에서 경로 입력·결과는 분리했지만 홈·상세·공용 지도는 아직 크다.
- Navigation Compose는 사용하지 않고 enum 화면 전환을 유지한다.
- 저장 장소는 기기 로컬이며 계정 간·기기 간 동기화하지 않는다.
- legacy 상대시간과 복수 route 코드는 호환을 위해 남아 있어 별도 제거 버전이 필요하다.

## 6. 회귀 기록 형식

실기기 확인 시 날짜, 기기/OS, APK commit, 환경(Preview/Production), 단계, 기대/실제,
스크린샷 또는 로그를 함께 남긴다. 자동 테스트 성공을 실기기 완료로 대체하지 않는다.
