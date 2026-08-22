# 틈새(Tteumsae)

강원도 여행 중 이동과 약속 사이에 남는 시간 안에서 들를 수 있는 장소를 추천하는 Android 앱입니다.

## 현재 기준선

- 기준일: `2026-08-22`
- Android 앱: `0.12.4` (`versionCode 25`)
- Android 패키지: `com.tteumsae.app`
- 제품 단계: 실제 API와 지도를 연결한 기능형 MVP
- 출시 단계: 테스터용 디버그 APK 가능, Play Store 제출 준비 전
- 운영 백엔드: <https://tteumsae-backend.vercel.app>
- APK 페이지: <https://tteumsae-apk.vercel.app>

현재 Android 소스와 `v0.12.4` 확인용 APK는 같은 기능 기준선입니다. 확인용
APK는 디버그 빌드이므로 Play Store 제출 전에는 릴리스 서명과 AAB 빌드, 실기기
회귀 검증을 별도로 완료해야 합니다.

## 저장소 구성

```text
android/   Kotlin·Jetpack Compose Android 앱
backend/   Vercel Functions·TourAPI·Kakao·Supabase 백엔드
download/  테스터용 APK 다운로드 페이지
design/    디자인 시스템과 화면 흐름 참고자료
docs/      제품·구현·운영 인수인계 문서
```

## 먼저 읽을 문서

1. [개발 시작 안내](docs/00_START_HERE.md)
2. [제품 범위](docs/01_PRODUCT_AND_SCOPE.md)
3. [아키텍처](docs/02_ARCHITECTURE.md)
4. [기능 구현표](docs/03_FEATURE_MATRIX.md)
5. [화면 흐름](docs/04_SCREEN_FLOWS.md)
6. [API와 데이터](docs/05_API_AND_DATA.md)
7. [환경과 접근 권한](docs/06_ENVIRONMENT_AND_ACCESS.md)
8. [빌드·테스트·배포](docs/07_BUILD_TEST_DEPLOY.md)
9. [QA와 알려진 문제](docs/08_QA_AND_KNOWN_ISSUES.md)
10. [다음 버전 계획](docs/09_NEXT_VERSION_PLAN.md)
11. [결정 기록](docs/10_DECISION_LOG.md)
12. [공모전 90초 데모와 실기기 QA](docs/11_CONTEST_DEMO_AND_DEVICE_QA.md)
13. [공모전 사용자·알고리즘 검증 양식](docs/12_CONTEST_VALIDATION_TEMPLATES.md)

## 빠른 검증

Android:

```powershell
cd android
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat compileDebugKotlin compileDebugUnitTestKotlin assembleDebug
```

백엔드:

```powershell
cd backend
corepack enable
pnpm install --frozen-lockfile
pnpm test
pnpm run check
```

실제 비밀값, `local.properties`, `.env.local`, Vercel 연결 정보, 키스토어와 APK는 Git에 포함하지 않습니다.
