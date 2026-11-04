# 카페 메뉴 관리 서비스 (Grids & Circle)

카테고리·메뉴·주문·배송 묶음·통계 API를 제공하는 Spring Boot 백엔드입니다.

## 1. 팀원별 담당 작업

| 팀원 | 담당 | 작업 내용 |
|---|---|---|
| 이동근 | 카테고리 | [내용] |
| 이재민 | 메뉴 | [내용] |
| 이지원 | 주문 | [내용] |
| 천승혁 | 통계·배송 묶음 / 에러 처리 | [내용] |

공동 작업: [예: 공통 예외 처리, DB 스키마 설계, requests/api-check.http 작성 등 누가 무엇을 했는지]

## 2. 구현 현황
### 완료

필수 API 24개(A01~A24)를 모두 구현했습니다.

| 구분 | 번호 | 상태 |
|---|---|---|
| 카테고리 | A01 ~ A05 | 완료 |
| 메뉴 | A06 ~ A10, A15, A19 ~ A21 | 완료 |
| 주문 | A11 ~ A13, A16, A22, A23 | 완료 |
| 배송 묶음 | A14 | 완료 |
| 통계 | A17, A18, A24 | 완료 |

### 미완료

없음

### 미해결 오류

없음

## 3. 실행 환경

| 항목 | 버전 |
|---|---|
| Java | 21 
| Spring Boot | 4.1.2 (Spring Web MVC, Spring Data JPA, Validation)|
| SQL | MySQL 8.4.11 (Docker 29.9.2, mysql:8.4 이미지)|
| 빌드 도구 | Gradle 9.8.2 |

## 4. 디렉토리 구조 요약
```text
NBE13-15-1-team02/
├── src/main/java            # 백엔드 코드 (위 패키지 구조)
├── src/main/resources/
│   ├── application.yaml     # DB 접속 정보 (환경변수 주입, 2.1 참고)
│   ├── schema.sql           # 테이블 생성 DDL
│   └── data.sql             # 초기 데이터 (카테고리 2개, 메뉴 4개)
├── src/test/java            # Controller 자동 테스트 (3.2 참고)
├── requests/
│   └── api-check.http       # API 확인 요청 A01~A24 + 오류 6건 (3.1 참고)
├── web/                     # 제공 화면, 8000 포트로 별도 실행 (2.4 참고)
└── build.gradle
```

## 5. 실행 방법

### 5-1. DB 준비 (새 DB)
```sql
CREATE DATABASE `kdt-spring` DEFAULT CHARACTER SET utf8mb4;
```
앱을 실행하면 `schema.sql`로 테이블이 만들어지고, `data.sql`로 초기 데이터(카테고리 2개, 메뉴 4개)가 등록됩니다. 다시 실행해도 중복 등록되지 않습니다.

### 5-2. 백엔드 실행
```bash
./gradlew bootRun
```
주소: `http://127.0.0.1:8080`

### 5-3. 제공 화면(web) 실행
```bash
cd web
java -m jdk.httpserver -b 127.0.0.1 -p 8000
```
`http://127.0.0.1:8000/` 접속 → 연결 설정 → 실제 API 연결 → API 서버 주소 `http://127.0.0.1:8080` (끝에 `/api/v1`을 붙이지 않음)

## 6. 테스트

- 자동 테스트 실행: `./gradlew test`
- `requests/api-check.http`: 정상 24개 + 오류 6개
  - 변수: `baseUrl`, [카테고리·메뉴·주문 ID 변수]
  - 실행 순서: [예: 카테고리 → 메뉴 → 주문 → 배송 묶음 → 통계]
- **시각 경계값 확인(13:59:59 / 14:00:00)**: [팀이 쓴 방법. 예: 시계를 주입해 테스트하며 실제 PC 시계는 바꾸지 않음]

## 7. 평가 대상

- 브랜치 / 태그 / 커밋: [예: main, 커밋 해시]
- 선택 심화: [구현한 항목과 코드·검증 방법. 안 했으면 "없음"]
