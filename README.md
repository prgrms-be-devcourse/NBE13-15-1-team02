# Grids & Circle 카페 메뉴 관리 서비스

> 명세 README 필수 항목 대응표 — 작성하면서 체크

| # | 명세 필수 내용 | 위치 | 작성 |
|:---:|:---|:---|:---:|
| 1 | 팀원별 담당 작업, 공동 구현 시 각자의 작업 내용 | 4. 팀 | ⬜ |
| 2 | 완료·미완료 기능과 미해결 오류 유무 (없으면 없다고 표시) | 5. 구현 현황 | ⬜ |
| 3 | 실제 Java·Spring Boot·MySQL 버전, DB 설정 위치/환경변수 이름과 예시 값 | 1. 소개, 2.1 환경 변수 | ⬜ |
| 4 | 새 DB 준비·초기 데이터 등록 절차, 백엔드·web 실행 폴더·명령·주소 | 2.2 ~ 2.4 | ⬜ |
| 5 | 테스트 실행 명령, .http 경로·변수·실행 순서, 시각 경계값 확인 방법 | 3. 검증 | ⬜ |
| 6 | 평가 대상 브랜치·태그 또는 커밋, 선택 심화 코드·검증 방법 | 6. 제출 정보 | ⬜ |

※ 실제 비밀번호·API 키는 저장소에 올리지 않는다.

---

## 1. 프로젝트 소개
- Grids & Circle 카페의 원두 주문, 상점 관리 백엔드입니다.
- Spring Boot로 카테고리·메뉴·주문 API 24개를 구현하고, 제공된 웹 화면에 연결했습니다.

### 기술 스택
- Java 21
- Spring Boot 4.1.2 (Spring Web MVC, Spring Data JPA, Validation)
- MySQL 8.4.11 (Docker 29.9.2, `mysql:8.4` 이미지)
- Gradle 9.8.2
- Lombok, JUnit 5

### 저장소 구조 요약

```
NBE13-15-1-team02
├── src/main/java          # 백엔드 코드 (아래 패키지 구조)
├── src/main/resources
│   ├── application.yaml   # DB 접속 정보 (환경변수 주입, 2.1 참고)
│   ├── schema.sql         # 테이블 생성 DDL
│   └── data.sql           # 초기 데이터 (카테고리 2개, 메뉴 4개)
├── src/test/java          # Controller 자동 테스트 (3.2 참고)
├── requests
│   └── api-check.http     # API 확인 요청 A01~A24 + 오류 6건 (3.1 참고)
├── web                    # 제공 화면, 8000 포트로 별도 실행 (2.4 참고)
└── build.gradle
```

### 패키지 구조 요약
도메인별 패키지 안에 Controller·Service·Repository·Entity·DTO를 함께 둡니다.

```
src/main/java/com/kdt/cafe
├── CafeApplication.java
├── category    # 카테고리 CRUD, 카테고리별 메뉴 (A01~A05, A15)
├── menu        # 메뉴 CRUD, 판매 중 목록·검색·판매 여부 변경 (A06~A10, A19~A21)
├── order       # 주문 접수·조회·취소·메모·재주문, 배송 묶음, 통계 (A11~A14, A16~A18, A22~A24)
│               # ShippingDateCalculator (서울 14시 기준 배송일)
├── common      # 업무 예외 클래스, 공통 오류 응답 ApiError, 전역 핸들러 ApiExceptionAdvice
└── config      # WebConfig(CORS), JpaAuditingConfig
```

## 2. 실행 방법
### 2.1 환경 변수 (필수 3)
- 변수 이름과 예시 값 표 (DB_URL, DB_USERNAME, DB_PASSWORD 등)
- application.yaml에서 읽는 위치
### 2.2 DB 준비 (필수 4)
- MySQL DB 생성 → schema.sql → data.sql 순서, 재실행 시 중복 안 되는 이유
### 2.3 백엔드 실행 (필수 4)
- `./gradlew bootRun`, 포트 8080
### 2.4 화면 실행 (필수 4)
- `web` 폴더에서 `java -m jdk.httpserver ...`, 접속 주소, 연결 설정 방법

## 3. 검증 (필수 5)
### 3.1 requests/api-check.http
- 변수 설명, 실행 순서, 준비 데이터
### 3.2 자동 테스트
- `./gradlew test`, 테스트 6개가 각각 확인하는 것
### 3.3 시각 경계값 확인 방법
- 13:59:59 / 14:00:00을 Clock으로 검증하는 방법

## 4. 팀 (필수 1)
- 팀원별 담당 표 (추적표 요약 또는 링크)

## 5. 구현 현황 (필수 2)
- 완료·미완료 기능, 미해결 오류 (없으면 "없음")

## 6. 제출 정보 (필수 6)
- 평가 대상 브랜치·태그·커밋
- 선택 심화 구현 여부와 검증 방법 (안 했으면 "없음")

## 참고 문서
- docs/요구사항 추적표, ERD 링크





























