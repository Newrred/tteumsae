# 틈새 개발 인수인계

작성일: 2026-08-03  
현재 앱 버전: `0.10.1` (`versionCode 18`)  
Android 패키지: `com.tteumsae.app`

이 저장소는 다른 Windows 컴퓨터에서 틈새 개발을 이어가기 위한 소스와 필수 자료를 모은 단일 저장소입니다. API 키, 로컬 SDK 경로, 서명키는 포함하지 않습니다.

## 폴더 구성

```text
source/
├─ tteumsae-android/   Android 앱
├─ tteumsae-backend/   Vercel Functions 백엔드
└─ tteumsae-download/  APK 다운로드 페이지
design/
├─ design-system-v1.0.svg
└─ figma-flow-2026-07-29.mp4
docs/
└─ CURRENT_STATUS.md
```

빌드 캐시, 과거 APK, `.vercel`, `.env.local`, `local.properties`, 키스토어는 용량과 보안을 위해 제외했습니다.

## 새 컴퓨터 준비

- Git
- Android Studio 및 JDK 17
- Android SDK Platform 35와 SDK Build Tools
- Node.js 20 이상

권장 복제 경로는 한글과 공백이 없는 `C:\dev\tteumsae`입니다. 현재 한글 경로에서는 Gradle JUnit 실행기가 `ClassNotFoundException`을 일으킨 이력이 있습니다.

프로젝트 주요 버전:

- Gradle 8.9
- Android Gradle Plugin 8.7.3
- Kotlin 2.0.21
- Jetpack Compose BOM 2024.12.01
- Kakao Maps Android SDK 2.14.0
- Android `minSdk 26`, `compileSdk 35`, `targetSdk 35`

## Android 실행

1. Android Studio에서 `source/tteumsae-android`를 엽니다.
2. `local.properties.example`을 참고해 같은 위치에 `local.properties`를 만듭니다.
3. `sdk.dir`과 `KAKAO_MAP_NATIVE_APP_KEY`를 입력합니다.
4. Gradle Sync 후 `app` 구성을 빌드합니다.

명령줄 검증:

```powershell
cd C:\dev\tteumsae\source\tteumsae-android
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat compileDebugKotlin compileDebugUnitTestKotlin
.\gradlew.bat assembleDebug
```

생성 APK: `app\build\outputs\apk\debug\app-debug.apk`

운영 API 주소는 `app/build.gradle.kts`의 `BuildConfig.API_BASE_URL`에 설정되어 있으며 현재 `https://tteumsae-backend.vercel.app`입니다.

### 카카오맵 디버그 키 해시

새 컴퓨터에서 생성된 디버그 키스토어는 키 해시가 달라질 수 있습니다. 다음 중 하나를 선택합니다.

1. 기존 컴퓨터의 `%USERPROFILE%\.android\debug.keystore`를 보안 저장소로 별도 전송합니다.
2. 새 키스토어의 디버그 키 해시를 Kakao Developers의 앱 설정에 추가합니다.

키스토어는 Git에 올리지 마세요. 공식 안내: https://developers.kakao.com/docs/ko/android/getting-started

## 백엔드 실행 및 배포

백엔드 의존성은 `pnpm-lock.yaml`로 고정했습니다.

```powershell
cd C:\dev\tteumsae\source\tteumsae-backend
corepack enable
pnpm install --frozen-lockfile
pnpm test
pnpm run check
```

Vercel 연결이 필요할 때만 실행합니다.

```powershell
pnpm dlx vercel login
pnpm dlx vercel link
pnpm dlx vercel env pull .env.local
```

Vercel 팀 `jaturi`의 `tteumsae-backend` 프로젝트를 선택합니다. 운영 환경변수는 Vercel에 보관되어 있으므로 Git에 넣지 않습니다.

운영 배포:

```powershell
pnpm dlx vercel --prod
```

## 필요한 비밀 설정

| 위치 | 이름 | 용도 |
|---|---|---|
| Android `local.properties` | `KAKAO_MAP_NATIVE_APP_KEY` | 네이티브 지도 표시 |
| Vercel | `TOUR_API_SERVICE_KEY` | 한국관광공사 TourAPI |
| Vercel | `KAKAO_REST_API_KEY` | 장소 검색·행정구역·차량 경로 |
| Vercel | `SUPABASE_URL` | 장소 데이터베이스 |
| Vercel | `SUPABASE_SERVICE_ROLE_KEY` | 서버 전용 DB 접근 |
| Vercel | `CRON_SECRET` | 동기화 Cron 보호 |

실제 키 값은 이 저장소에 없습니다. Vercel, Kakao Developers, 공공데이터포털에서 확인하거나 재발급하세요.

## 운영 주소

- 백엔드: https://tteumsae-backend.vercel.app
- APK 다운로드 페이지: https://tteumsae-apk.vercel.app
- 현재 테스트 APK: https://tteumsae-apk.vercel.app/tteumsae-v0.10.1-debug.apk

## 새 컴퓨터에서 확인할 순서

1. 저장소를 `C:\dev\tteumsae`에 복제
2. Android Studio와 SDK 35 설치
3. `local.properties` 생성
4. 카카오 네이티브 키와 디버그 키 해시 확인
5. Android 컴파일과 `assembleDebug` 실행
6. 실제 기기에 APK 설치 후 지도·GPS 확인
7. 백엔드 `pnpm test` 및 `pnpm run check` 실행
8. 필요할 때만 Vercel 프로젝트 연결
9. [현재 개발 상태](docs/CURRENT_STATUS.md)의 다음 우선순위부터 작업
