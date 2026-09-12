# 수동 출시 검증·촬영용 흐름

이 폴더는 `.maestro/config.yaml`의 기본 `flows/*.yaml`에서 제외한다.
실제 운영 장소·기존 로그인/저장 상태·특정 시각을 전제로 한 출시 QA와 캡처 도구이며,
전체 폴더를 일반 smoke처럼 연속 실행하지 않는다.

기존 `.maestro/flows/`에도 `gps-denied-kakao.yaml` 등 자동 위치 전용 흐름이 남아 있다.
따라서 기본 전체 폴더 실행이 수동 APK에 호환된다는 뜻이 아니며, 수동 검증은 아래
준비 조건을 갖춘 파일을 명시적으로 선택한다.

- `manual-release-input`: 출발지 수동 입력·추천 준비.
- `manual-release-detail`, `manual-release-saved-settings`: 준비된 결과/저장 화면에서 왕복 확인.
- `manual-release-login-handoff`: 카카오 로그인 화면까지만 연다. 계정 동의는 직접 확인한다.
- `manual-release-notices-and-tourism`: code32의 기존 카카오 로그인과 순포해변 저장 상태가 필요하다.
- `manual-tourism-*`: 2026-09-13 관광 예시 촬영. 특히 capture는 06시대 도착 마감과
  해당 시각의 추천에 의존하므로 다른 시간에 장소가 빠지는 것을 앱 오류로 판정하지 않는다.

각 파일의 주석과 시작 조건을 읽고 한 파일만 명시적으로 실행한다. 실행 결과는
`docs/08_QA_AND_KNOWN_ISSUES.md`와 Git 제외 `tmp/release-qa-final-20260913/`에 구분한다.
기존 알림을 관찰 중일 때 앱 강제 재시작·새 경로 검색·다른 장소 선택을 하지 않는다.
이는 알림 취소나 재예약을 유발할 수 있다. 사용자 계정/저장 데이터 삭제는 하지 않는다.
