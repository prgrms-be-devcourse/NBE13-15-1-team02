# Grids & Circle 카페 메뉴 관리 서비스

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
│   └── data.sql           # 초기 데이터 (카테고리 2개, 메뉴 4개) + 테스트용 주문 1건
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

- 사전 준비 : JDK21, Docker, Git

### 2.1 환경 변수
DB 접속 정보는 프로젝트 루트의 `.env`에서 읽습니다.<br>
`src/main/resources/application.yaml`이 `spring.config.import`로 `.env`를 불러오고, <br>
`spring.datasource`의 `url` · `username` · `password`가 각각 `${DB_URL}` · `${DB_USERNAME}` · `${DB_PASSWORD}`를 사용합니다.

`.env`는 저장소에 포함되지 않으므로 예시 파일(`.env.example`)을 복사해 비밀번호를 채웁니다.<br>
`DB_URL`의 포트를 내 Docker MySQL 포트에 맞춰 바꿔주세요. (예: `-p 3307:3306` -> `3307`)

```bash
cp .env.example .env        # Windows: copy .env.example .env
```

| 변수 | 설명 | 예시 값 |
|:---|:---|:---|
| `DB_URL` | JDBC 접속 주소  | `jdbc:mysql://127.0.0.1:3306/kdt-spring?allowPublicKeyRetrieval=true&sslMode=DISABLED` |
| `DB_USERNAME` | DB 사용자  | `root` |
| `DB_PASSWORD` | DB 비밀번호 | `your_password` |

### 2.2 DB 준비

**1) MySQL 컨테이너 실행**

컨테이너를 만들 때 비밀번호와 포트는 `.env`와 같게 입력합니다.<br>
(비밀번호 = `DB_PASSWORD`, 포트 = `DB_URL`의 포트 번호)

Docker Desktop의 Containers 목록에 `kdt-mysql`이 이미 있다면 새로 만들지 않고 실행만 합니다.<br>
(명령어: `docker start kdt-mysql` / Docker Desktop: ▶ 버튼)

**방법 A. 명령어(Windows 동일)**

```bash 
docker run -d --name kdt-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=your_password -e MYSQL_DATABASE=kdt-spring mysql:8.4
```

**방법 B. Docker Desktop**

1. 상단 검색창에 `mysql` 입력 -> `mysql` 이미지 선택 -> Tag `8.4` -> **Pull**
2. **Images** 탭에서 `mysql:8.4` 옆 ▶(Run) 클릭 -> **Optional settings** 펼치기
3. 아래 값을 입력 후 **Run**

| 항목 | 값 |
|:---|:---|
| Container name | `kdt-mysql` |
| Host port | `3306` (내 `DB_URL` 포트) |
| Environment variables | `MYSQL_ROOT_PASSWORD` = `your_password` |
|  | `MYSQL_DATABASE` = `kdt-spring` |

**실행 확인**: Containers 탭(또는 `docker ps`)에서 `kdt-mysql`이 실행 중이면 성공입니다.

**2) 테이블 생성 · 초기 데이터 등록(IntelliJ Database 도구)**

1. 오른쪽 **Database** 탭 -> `+` -> Data Source -> MySQL
2. Host `127.0.0.1`, Port `3306`(내 Docker 포트), User `root`, Password는 `.env`의 `DB_PASSWORD`, Database `kdt-spring` 입력 후 **Test Connection** -> OK
3. 프로젝트 트리에서 `src/main/resources/schema.sql` 우클릭 -> **Run `schema.sql`** -> 방금 만든 데이터 소스 선택
4. 같은 방법으로 `src/main/resources/data.sql` 실행

`schema.sql`과 `data.sql`은 여러 번 실행해도 안전합니다.<br>
(테이블이 이미 있으면 건너뛰고, 초기 데이터는 이미 있으면 다시 등록하지 않습니다.)

**3) 확인**

Database 탭에서 `kdt-spring` 우클릭 -> **New -> Query Console**을 열고 아래를 실행합니다.<br>
카테고리 2개(싱글오리진, 블렌드), 메뉴 4개, 테스트용 주문 1건이 조회되면 성공입니다.

```sql
USE `kdt-spring`;
SELECT * FROM category;
SELECT id, category_id, name, price, available FROM menu;
SELECT id, email, status, shipping_date FROM `order`;
```

테스트용 주문(`past@example.com`)은 3.2의 E5에서 사용하는 취소 마감이 지난 주문입니다.

### 2.3 백엔드 실행 

2.2의 `kdt-mysql` 컨테이너가 실행 중이고, 프로젝트 루트에 `.env`가 있어야 합니다.

**방법 A. IntelliJ**

`src/main/java/com/kdt/cafe/CafeApplication.java`를 열고 `main` 메서드 옆 ▶ 버튼 -> **Run `CafeApplication`**

**방법 B. 터미널**

프로젝트 루트에서 실행합니다.

```bash
./gradlew bootRun             # Windows: .\gradlew bootRun
```

**실행 확인**

로그에 `Started CafeApplication`이 나오면 서버가 `8080` 포트로 실행된 것입니다.<br>
브라우저에서 `http://127.0.0.1:8080/api/v1/categories` 에 접속해 아래와 같은 JSON이 보이면 DB 연결까지 성공입니다.

```json
[{"id": 1, "name": "싱글오리진"},{"id": 2, "name": "블렌드"}]
```

### 2.4 화면 실행 

2.3의 백엔드가 실행 중이어야 합니다.

**1) 화면 서버 실행**

백엔드와 다른 **새 터미널**을 열고, 프로젝트 루트에서 `web` 폴더로 이동해 실행합니다.

```bash
cd web
java -m jdk.httpserver -b 127.0.0.1 -p 8000
```

**2) 브라우저 접속**

`http://127.0.0.1:8000`에 접속합니다.<br>
`localhost:8000`으로 접속하면 브라우저가 다른 주소로 취급해 API 요청이 차단되므로, 반드시 `127.0.0.1`을 사용합니다.

**실행 확인**

첫 화면인 원두 주문 화면에 초기 메뉴 4개가 보이면 성공입니다.<br>
연결되지 않으면 화면의 **연결 설정 -> 실제 API 연결**에서 서버 주소가 `http://127.0.0.1:8080`인지 확인합니다.

## 3. 검증
### 3.1 테스트 실행

프로젝트 루트에서 실행합니다.

```bash
./gradlew test        # Windows: .\gradlew test
```

특정 테스트만 실행할때는 `--tests`로 클래스를 지정합니다.

```bash
./gradlew test --tests "com.kdt.cafe.category.CategoryApiContorller"    # MacOS·Linux
.\gradlew test --tests "com.kdt.cafe.category.CategoryApiContorller"    # Windows
```

Controller 테스트는 모의 Service를 사용해 DB 연결 없이 실행됩니다.

| 분류 | 테스트 | 확인하는 요구사항 |
|:---|:---|:---|
| 정상 처리 | `CategoryApiControllerTest.registerCategory` | 카테고리 등록(A03)은 201과 생성된 카테고리의 id·name을 반환한다 |
| 정상 처리 | `CategoryApiControllerTest.deleteCategory`| 카테고리 삭제(A05)는 204와 빈 본문을 반환한다 |
| 입력 오류 | `MenuApiControllerTest.changeAvailable_without_available_returns_400` | 판매 여부 변경(A21)에 `available`이 없으면 400 `VALIDATION_ERROR`와 `fieldErrors.available`을 반환한다 |
| 입력 오류 | `MenuApiControllerTest.findOne_with_non_numeric_id_returns_400` | 메뉴 상세(A07)의 ID가 숫자가 아니면 400 `VALIDATION_ERROR`와 `fieldErrors.menuId`를 반환한다 |
| 대상 없음 | `OrderApiControllerTest.order_not_exist_is_404` | 없는 주문을 상세 조회(A13)하면 404 `ORDER_NOT_FOUND`와 해당 메세지를 반환한다 |
| 업무 위반 | `OrderApiControllerTest.cancel_after_deadline_is_409` | 취소 마감이 지난 주문을 취소(A16)하면 409 `ORDER_NOT_CANCELLABLE`과 해당 메세지를 반환한다 |


### 3.2 API 확인 요청 (`requests/api-check.http`)

IntelliJ HTTP Client로 실행합니다. 2.3의 벡엔드가 시랳ㅇ중이어야 하며, `data.sql`을 실행한 상태에서 시작합니다.<br>
파일을 열고 위에서 아래로 각 요청 옆 ▶ 버튼을 눌러 순서대로 실행하거나, 상단 **Run All**로 전체를 순서대로 실행합니다.

**변수**

ID는 위에서부터 순서대로 실행했을 때 생성되는 번호를 기준으로 미리 정해 두었습니다.

| 변수 | 값 | 사용처 |
|:---|:---|:---|
| `baseUrl` | `http://127.0.0.1:8080` | 모든 요청 |
| `categoryId` | `1` | A02, A15 (초기 카테고리 조회) |
| `categoryIdDump` | `3` | A04, A05 (A03에서 새로 등록한 카테고리) |
| `menuId` | `3` | A07 (초기 메뉴 조회) |
| `menuIdDump` | `5` | A09, A10 (A08에서 새로 등록한 메뉴) |
| `unavailableMenuId` | `4` | A21, E4 (판매 중지 대상 메뉴) |
| `emailDump` · `emailDump2` · `emailDump3` | 테스트용 이메일 | A11 및 추가 주문 2건, A12 |
| `orderId` | `2` | A13, A22, A23 (A11에서 접수한 주문) |
| `cancelOrderId` | `4` | A16 (취소 가능한 주문) |
| `pastOrderId` | `1` | E5 (data.sql로 등록된 취소 마감이 지난 주문) |
| `shippingDate` | `2026-11-06` | A14 (조회할 배송일) |
| `today` | `2026-11-05` | A17, A18, A24 (조회할 접수일) |

**실행 순서**

1. **정상 요청 A01 → A24**를 순서대로 실행합니다. 단, **A21(판매 중지)은 A23(재주문) 뒤**에 실행합니다. 재주문할 원본 주문에 판매 중지 메뉴가 없어야 하기 때문입니다.
2. **오류 요청 E1 → E6**을 실행합니다.
3. 마지막 **정리** 요청으로 A21에서 판매 중지한 메뉴를 판매 가능으로 되돌립니다.

| 번호 | 대상 API | 상황 | 기대 결과 |
|:---:|:---|:---|:---|
| E1 | A17 | 허용 범위(2000-01-01 ~ 2099-12-31) 밖 날짜 | 400 `VALIDATION_ERROR` |
| E2 | A03 | 카테고리명 30자 초과 | 400 `VALIDATION_ERROR`, `fieldErrors.name` |
| E3 | A07 | 존재하지 않는 메뉴 조회 | 404 `MENU_NOT_FOUND` |
| E4 | A11 | 판매 중지 메뉴로 주문 (A21 실행 후) | 409 `MENU_UNAVAILABLE` |
| E5 | A16 | 취소 마감(배송일 14:00:00)이 지난 주문 취소 | 409 `ORDER_NOT_CANCELLABLE` |
| E6 | A10 | 주문에 사용된 메뉴 삭제 | 409 `MENU_IN_USE` |

각 요청 위 주석(`###`)에 기대 상태 코드와 확인할 응답 필드를 적어 두었습니다.

**다시 실행할 때**

실행하면 카테고리·메뉴·주문이 새로 생기고 ID가 바뀌므로, 다시 실행하기 전에 테이블을 지우고 2.2의 `schema.sql` → `data.sql`부터 실행해 처음 상태로 되돌립니다.


### 3.3 시각 경계값 확인 방법
- 13:59:59 / 14:00:00을 Clock으로 검증하는 방법

## 4. 팀 

### 팀원별 담당 작업

| 이름 | 담당 작업 |
|:---|:---|
| 천승혁 | "여기에 작성해주세요" |
| 이재민 | "메뉴 API(A06 ~ A10, A19 ~ A21) - menu 패키지 전체, 발표 PPT 제작" |
| 이지원 | "여기에 작성해주세요" |
| 이동근 | 카테고리 API(A01~A05, A15) - category 패키지 전체, webConfig 설정 파일, README.md 작성|

### 공동 구현 시 각자의 작업 내용
| 공동 구현 항목 | 이름 | 각자의 작업 내용 |
|:---|:---|:---|
| DB 설계 | 전원 | 요구사항을 함께 대조하며 테이블 · 제약 조건 결정|




## 5. 구현 현황 

### 기능

✅ 완료 · ⬜ 미완료

| 번호 | 기능 | 상태 | 
|:---:|:---|:---:|
| A01 | 카테고리 목록 | ✅ | 
| A02 | 카테고리 상세 | ✅ | 
| A03 | 카테고리 등록 | ✅ | 
| A04 | 카테고리 수정 | ✅ | 
| A05 | 카테고리 삭제 | ✅ | 
| A06 | 전체 메뉴 | ✅ | 
| A07 | 메뉴 상세 | ✅ | 
| A08 | 메뉴 등록 | ✅ | 
| A09 | 메뉴 일발 수정 | ✅ | 
| A10 | 메뉴 삭제 | ✅ | 
| A11 | 주문 접수 | ✅ | 
| A12 | 이메일별 주문 | ✅ | 
| A13 | 주문 상세 | ✅ | 
| A14 | 배송 묶음 | ✅ | 
| A15 | 카테고리별 메뉴 | ✅ | 
| A16 | 주문 취소 | ✅ | 
| A17 | 접수일별 주문 통계 | ✅ | 
| A18 | 접수일별 메뉴 통계 | ✅ | 
| A19 | 판매 중 메뉴 | ✅ | 
| A20 | 메뉴명 검색 | ✅ | 
| A21 | 판매 여부 변경 | ✅ | 
| A22 | 주문 메모 수정 | ✅ | 
| A23 | 재주문 | ✅ | 
| A24 | 접수일별 고객 통계 | ✅ |


### 미해결 오류

없음


### 선택 심화
| 항목 | 상태 |
|:---|:---:|
| 업무 규칙 자동 테스트 | ⬜ |
| 실제 DB 저장·롤백 테스트 | ⬜ |
| Redis 메뉴 조회 캐시 | ✅  |
| 메뉴 페이징 API | ⬜ |
| RestClient 외부 조회 | ⬜ |

## 6. 제출 정보 (필수 6)

### 평가 대상

- 브랜치: `main`
- 태그: `submit-v1`

git checkout main          # 평가 대상 브랜치에 있는지 확인 <br>
git commit -m "최종 제출"   # 마지막 커밋 <br>
git push origin main       # 브랜치 먼저 푸시 <br>
git tag submit-v1          # 지금 커밋에 태그 <br>
git push origin submit-v1  # 태그 푸시 <br>

### 선택 심화

- 없음
- 한다면 예시는 아래에
- 
**Redis 메뉴 조회 캐시** (담당: 이재민)
- 대상: A06 전체 메뉴 (`GET /api/v1/menus`)
- 코드: `src/main/java/com/kdt/cafe/menu/MenuListCache.java`, `src/main/java/com/kdt/cafe/menu/MenuService.java`
- 사전 준비: Redis 컨테이너 실행 <br>
  `docker run -d --name kdt-redis -p 6379:6379 redis:7` (이미 있으면 `docker start kdt-redis`)
- 자동 테스트: `src/test/java/com/kdt/cafe/menu/MenuServiceCacheTest.java`
- 검증: `./gradlew test --tests "*MenuServiceCacheTest*"`
- 수동 확인: `requests/api-check.http`의 C1~C12 (3.2의 정리 요청까지 실행한 뒤 실행) <br>
  `docker exec -it kdt-redis redis-cli --raw`로 접속해 `GET cafe:menus:all`, `TTL cafe:menus:all` 확인
- 확인 내용:
  - 캐시 키 `cafe:menus:all`, 값은 메뉴 배열 JSON, TTL 60초
  - 첫 조회는 캐시 MISS로 DB를 조회한 뒤 Redis에 저장하고, TTL 안에 다시 조회하면 캐시 HIT로 DB를 조회하지 않음
  - 메뉴 등록·수정·판매 여부 변경·삭제 시 트랜잭션 커밋 후 캐시를 삭제하고, 다음 조회에서 변경 내용이 반영되어 다시 저장됨
  
**업무 규칙 자동 테스트**
- 코드: `/src/test/java/com/kdt/cafe/order/shippingDateCalculatorTest.java`
- 검증: `./gradlew test --tests "*shippingDateCalculatorTest*"`
- 확인 내용: 





























