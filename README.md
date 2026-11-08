# Grids & Circle 카페 메뉴 관리 서비스

---

## 1. 프로젝트 소개
- Grids & Circle 카페의 원두 주문, 상점 관리 백엔드입니다.
- Spring Boot로 카테고리·메뉴·주문 API 24개를 구현하고, 제공된 웹 화면에 연결했습니다.

### 기술 스택
- Java 21
- Spring Boot 4.1.2 (Spring Web MVC, Spring Data JPA, Validation, Spring Data Redis)
- MySQL 8.4.11 (Docker 29.9.2, `mysql:8.4` 이미지)
- Redis 7 (Docker, `redis:7` 이미지, 선택 심화 메뉴 조회 캐시용)
- Gradle 9.8.2
- Lombok, JUnit 5

### 저장소 구조 요약

```
NBE13-15-1-team02
├── src/main/java          # 백엔드 코드 (아래 패키지 구조)
├── src/main/resources
│   ├── application.yaml   # DB·Redis 접속 정보 (환경변수 주입, 2.1 참고)
│   ├── schema.sql         # 테이블 생성 DDL
│   ├── data.sql           # 초기 데이터 (카테고리 2개, 메뉴 4개) + 테스트용 주문 1건
│   └── pagedata.sql       # 페이징 확인용 추가 메뉴 데이터 (6절 참고)
├── src/test/java          # Controller 자동 테스트, DB 저장·롤백 테스트 (3.1, 6절 참고)
├── src/test/resources
│   └── application-test.yaml   # 테스트 DB 설정 (kdt-spring-test)
├── requests
│   └── api-check.http     # API 확인 요청 A01~A24 + 오류 6건 (3.2 참고)
├── web                    # 제공 화면, 8000 포트로 별도 실행 (2.4 참고)
└── build.gradle
```

### 패키지 구조 요약
도메인별 패키지 안에 Controller·Service·Repository·Entity·DTO를 함께 둡니다.

```
src/main/java/com/kdt/cafe
├── CafeApplication.java
├── category    # 카테고리 CRUD, 카테고리별 메뉴 (A01~A05, A15)
├── menu        # 메뉴 CRUD, 판매 중 목록·검색·판매 여부 변경 (A06~A10, A19~A21), 페이징·Redis 캐시 MenuListCache(선택 심화)
├── order       # 주문 접수·조회·취소·메모·재주문, 배송 묶음, 통계 (A11~A14, A16~A18, A22~A24)
│               # ShippingDateCalculator (서울 14시 기준 배송일)
├── common      # 업무 예외 클래스, 공통 오류 응답 ApiError, 전역 핸들러 ApiExceptionAdvice
└── config      # WebConfig(CORS), JpaAuditingConfig, ClockConfig(서울 기준 Clock 빈)
```

## 2. 실행 방법

- 사전 준비 : JDK21, Docker, Git (Redis 캐시 확인 시에만 Redis 컨테이너 필요, 2.2 4) 참고)

### 2.1 환경 변수
DB 접속 정보는 프로젝트 루트의 `.env`에서 읽습니다.<br>
`src/main/resources/application.yaml`이 `spring.config.import`로 `.env`를 불러오고, <br>
`spring.datasource`의 `url` · `username` · `password`가 각각 `${DB_URL}` · `${DB_USERNAME}` · `${DB_PASSWORD}`를 사용합니다.

`.env`는 저장소에 포함되지 않으므로 예시 파일(`.env.example`)을 복사해 비밀번호를 채웁니다.<br>
`DB_URL`과 `TEST_DB_URL`의 포트를 내 Docker MySQL 포트에 맞춰 바꿔주세요. (예: `-p 3307:3306` -> `3307`)

```bash
cp .env.example .env        # Windows: copy .env.example .env
```

| 변수 | 설명 | 예시 값 |
|:---|:---|:---|
| `DB_URL` | JDBC 접속 주소  | `jdbc:mysql://127.0.0.1:3306/kdt-spring?allowPublicKeyRetrieval=true&sslMode=DISABLED` |
| `DB_USERNAME` | DB 사용자  | `root` |
| `DB_PASSWORD` | DB 비밀번호 | `your_password` |
| `TEST_DB_URL` | 테스트 전용 DB 접속 주소 (6절 참고) | `jdbc:mysql://127.0.0.1:3306/kdt-spring-test?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&sslMode=DISABLED` |
| `REDIS_HOST` | Redis 주소 (선택, 기본값 `127.0.0.1`) | `127.0.0.1` |
| `REDIS_PORT` | Redis 포트 (선택, 기본값 `6379`) | `6379` |
| `MENU_CACHE_TTL_SECONDS` | 메뉴 목록 캐시 TTL, 1 이상의 정수 초 (선택, 기본값 `60`) | `60` |

Redis 변수 3개는 기본값이 있어 `.env`에 없어도 동작합니다. Redis 설정은 6.3절을 참고하세요.

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

**4) Redis 컨테이너 실행 (선택 심화: 메뉴 조회 캐시 확인 시에만)**

```bash
docker run -d --name kdt-redis -p 6379:6379 redis:7
```

이미 `kdt-redis`가 있다면 `docker start kdt-redis`로 실행만 합니다.<br>
Redis가 꺼져 있어도 백엔드는 정상 동작하며, 캐시 없이 DB에서 바로 조회합니다.

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

특정 테스트만 실행할 때는 `--tests`로 클래스를 지정합니다.

```bash
./gradlew test --tests "com.kdt.cafe.category.CategoryApiControllerTest"    # MacOS·Linux
.\gradlew test --tests "com.kdt.cafe.category.CategoryApiControllerTest"    # Windows
```

Controller 테스트는 모의 Service를 사용해 DB 연결 없이 실행됩니다.<br>
전체 테스트(`./gradlew test`)에는 DB 저장·롤백 테스트가 포함되므로, 2.2의 MySQL 컨테이너와 `.env`의 `TEST_DB_URL`이 필요합니다.

| 분류 | 테스트 | 확인하는 요구사항 |
|:---|:---|:---|
| 정상 처리 | `CategoryApiControllerTest.registerCategory` | 카테고리 등록(A03)은 201과 생성된 카테고리의 id·name을 반환한다 |
| 정상 처리 | `CategoryApiControllerTest.deleteCategory`| 카테고리 삭제(A05)는 204와 빈 본문을 반환한다 |
| 입력 오류 | `MenuApiControllerTest.changeAvailable_without_available_returns_400` | 판매 여부 변경(A21)에 `available`이 없으면 400 `VALIDATION_ERROR`와 `fieldErrors.available`을 반환한다 |
| 입력 오류 | `MenuApiControllerTest.findOne_with_non_numeric_id_returns_400` | 메뉴 상세(A07)의 ID가 숫자가 아니면 400 `VALIDATION_ERROR`와 `fieldErrors.menuId`를 반환한다 |
| 대상 없음 | `OrderApiControllerTest.order_not_exist_is_404` | 없는 주문을 상세 조회(A13)하면 404 `ORDER_NOT_FOUND`와 해당 메세지를 반환한다 |
| 업무 위반 | `OrderApiControllerTest.cancel_after_deadline_is_409` | 취소 마감이 지난 주문을 취소(A16)하면 409 `ORDER_NOT_CANCELLABLE`과 해당 메세지를 반환한다 |


### 3.2 API 확인 요청 (`requests/api-check.http`)

IntelliJ HTTP Client로 실행합니다. 2.3의 백엔드가 실행 중이어야 하며, `data.sql`을 실행한 상태에서 시작합니다.<br>
파일을 열고 위에서 아래로 각 요청 옆 ▶ 버튼을 눌러 순서대로 실행하거나, 상단 **Run All**로 전체를 순서대로 실행합니다.

**변수**

ID는 위에서부터 순서대로 실행했을 때 생성되는 번호를 기준으로 미리 정해 두었습니다.<br>
shippingDate와 today는 실행하는 날짜에 맞춰 바꿉니다.

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

실제 PC·서버 시계는 바꾸지 않고, 자동 테스트에 고정 시각을 넣어 확인합니다.<br>
배송일 계산과 취소 가능 여부 판단은 `ShippingDateCalculator`가 현재 시각을 직접 구하지 않고 매개변수로 받아 처리하므로, 테스트에서 13:59:59 / 14:00:00을 그대로 넣어 검증할 수 있습니다.

```bash
./gradlew test --tests "com.kdt.cafe.order.ShippingDateCalculatorTest"     # MacOS·Linux
.\gradlew test --tests "com.kdt.cafe.order.ShippingDateCalculatorTest"     # Windows
```

| 구분 | 입력 시각 | 결과 |
|:---|:---|:---|
| 배송일 | 13:59:59 접수 | 당일 배송 |
| 배송일 | 14:00:00 접수 | 다음 날 배송 |
| 취소 가능 여부 | 배송일 13:59:59 | 취소 가능 |
| 취소 가능 여부 | 배송일 14:00:00 | 취소 불가 (API 응답: 409 `ORDER_NOT_CANCELLABLE`) |

Service를 거친 취소·재주문까지 포함한 전체 업무 규칙 테스트는 6.1절을 참고하세요.

## 4. 팀

### 팀원별 담당 작업

| 이름 | 역할 | 담당 API | 담당 작업 |
|:---|:---|:---|:---|
| 천승혁 | PM | A11\~A14, A16\~A18, A22\~A24<br>A06\~A10, A19\~A21 | 주문 기능 구현(`OrderController`·`OrderItem`)<br>공통·설정 패키지 작성(`common` 패키지·`config`)<br>메뉴 초안·API 테스트(`MenuService`·`MenuController` 초안, `api-check.http`)<br>별도 메뉴 페이징 API(심화) |
| 이동근 | 카테고리 | A01\~A05, A15 | `category` 패키지 전체 구현<br>`WebConfig` 설정 파일 작성<br>실제 DB 저장·롤백 테스트(심화)<br>`README.md` 작성 |
| 이지원 | 오더 | A11\~A14, A16\~A18, A22\~A24 | `Order` Entity·DTO·Service 구현<br>`api-check.http` 작성<br>`OrderApiControllerTest` 작성<br>업무 규칙 자동 테스트(심화) |
| 이재민 | 메뉴 | A06\~A10, A19\~A21 | `menu` 패키지 전체 구현<br>Redis 메뉴 조회 캐시(심화)<br>PPT 제작 |

### 공동 구현 시 각자의 작업 내용

| 공동 구현 항목 | 이름 | 각자의 작업 내용 |
|:---|:---|:---|
| DB 설계 | 전원 | 요구사항을 함께 대조하며 테이블·제약 조건 결정 |
| 주문 기능<br>(A11\~A14, A16\~A18, A22\~A24) | 천승혁 | `OrderController`, `OrderItem` 구현 |
| | 이지원 | `Order` Entity·DTO·Service 구현 |
| 메뉴 기능<br>(A06\~A10, A19\~A21) | 천승혁 | `MenuService`·`MenuController` 초안 작성 |
| | 이재민 | `menu` 패키지 전체 구현 |
| API 확인 요청<br>(`api-check.http`) | 천승혁 | 메뉴 API 테스트 요청 작성 |
| | 이지원 | `api-check.http` 작성 |


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
| A09 | 메뉴 일반 수정 | ✅ | 
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
| 업무 규칙 자동 테스트 | ✅ |
| 실제 DB 저장·롤백 테스트 | ✅ |
| Redis 메뉴 조회 캐시 | ✅ |
| 메뉴 페이징 API | ✅ |
| RestClient 외부 조회 | ⬜ |

## 6. 선택 심화

### 6.1 업무 규칙 자동 테스트

- 코드
  - `src/test/java/com/kdt/cafe/order/ShippingDateCalculatorTest.java`
  - `src/test/java/com/kdt/cafe/order/OrderTest.java`
  - `src/test/java/com/kdt/cafe/order/OrderServiceTest.java`
- 검증

```bash
./gradlew test --tests "com.kdt.cafe.order.*Test"     # MacOS·Linux
.\gradlew test --tests "com.kdt.cafe.order.*Test"     # Windows
```

- 확인 내용

| 테스트 | 확인하는 업무 규칙 |
|:---|:---|
| `ShippingDateCalculatorTest` | 배송일: 13:59:59 접수는 당일, 14:00:00·14:00:01 접수는 다음 날, 12-31 14:00:01 접수는 다음 해 01-01 |
| `ShippingDateCalculatorTest` | 취소 마감: 배송일 13:59:59까지 취소 가능, 14:00:00부터 불가 |
| `OrderTest` | 취소: 배송일 13:59:59 취소 성공(상태·취소 시각만 변경), 14:00:00 취소는 `ORDER_NOT_CANCELLABLE`이고 주문 그대로 유지 |
| `OrderTest` | 재취소: 마감 이후 재취소해도 성공하며 최초 취소 시각과 금액·항목 유지 |
| `OrderServiceTest` | 취소(Service): 멈춘 시계 13:59:59 / 14:00:00으로 Service를 통한 취소 성공 / `ORDER_NOT_CANCELLABLE` |
| `OrderServiceTest` | 재주문: 현재 메뉴 이름·가격으로 새 주문 계산, 원본 주문의 단가·항목 금액·총액 보존 |

- 실제 PC·서버 시계는 바꾸지 않습니다.<br>
  도메인(`ShippingDateCalculator`, `Order`)은 시각을 매개변수로 받아 고정 시각을 직접 넣고, `OrderService`는 생성자로 `Clock`을 주입받으므로(운영 시 `config/ClockConfig`의 서울 기준 `Clock` 빈 사용) 테스트에서는 `Clock.fixed`로 멈춘 시계를 넣어 검증합니다.
- `OrderServiceTest`는 Spring 없이 Repository만 `mock()`으로 대신하고, 계산과 판단은 실제 Service·`Order` 로직이 수행합니다.

### 6.2 실제 DB 저장·롤백 테스트

- 코드: `src/test/java/com/kdt/cafe/order/OrderDbSaveRollbackTest.java`
- 테스트 DB: 앱 실행용 `kdt-spring`과 분리된 `kdt-spring-test` 사용 (`src/test/resources/application-test.yaml`, `.env`의 `TEST_DB_URL`)<br>
  테스트 시작 시 `schema.sql`·`data.sql`이 자동 실행되고, DB가 없으면 자동 생성됩니다.
- 실행 전: 2.2의 MySQL 컨테이너 실행, `.env`에 `TEST_DB_URL` 설정

- 실행:

```bash
./gradlew test --tests "com.kdt.cafe.order.OrderDbSaveRollbackTest"    # MacOS·Linux
.\gradlew test --tests "com.kdt.cafe.order.OrderDbSaveRollbackTest"    # Windows
```

- 검증 흐름:
    1. 정상 주문 A → 실제 DB의 주문·항목 행에서 메뉴명·단가·수량·항목 금액·총액 확인
    2. 주문 B → 주문 행과 항목을 INSERT한 직후 강제 실패 (제품 코드 변경 없이 테스트의 스파이로 실패 발생)
    3. 롤백 확인 → DB에 A만 남고 B의 주문·항목 행 없음
    4. 정상 주문 C → 실패 이후에도 정상 저장 확인
- 매 실행 전 주문·항목 테이블을 비우므로 연속 실행해도 결과가 같습니다.
- 콘솔의 `[① ...]` ~ `[④ ...]` 줄에서 단계별 DB 상태를 확인할 수 있습니다.

### 6.3 Redis 메뉴 조회 캐시

- 대상: A06 전체 메뉴 (`GET /api/v1/menus`)
- 코드
    - `src/main/java/com/kdt/cafe/menu/MenuListCache.java`: Redis 캐시 조회·저장·삭제
    - `src/main/java/com/kdt/cafe/menu/MenuService.java` (`findAll`): 캐시가 있으면 캐시 반환, 없으면 DB 조회 후 저장
- 동작
    - 캐시 키 `cafe:menus:all`, 값은 메뉴 목록 JSON, TTL 60초(`MENU_CACHE_TTL_SECONDS`로 변경, 1 이상의 정수 초)
    - 첫 조회는 DB를 조회해 Redis에 저장하고, TTL 안의 재조회는 DB 없이 Redis 값을 반환합니다.
    - 메뉴 등록·수정·판매 여부 변경·삭제 시 커밋 후 캐시를 삭제해, 다음 조회에 변경 내용이 반영됩니다.
    - Redis 오류가 나도 예외 없이 DB 조회로 대체됩니다.
    - 기존 A06의 응답 형식은 그대로 유지합니다.
- 실행 전: Redis 컨테이너 실행 (2.2의 4))

```bash
docker run -d --name kdt-redis -p 6379:6379 redis:7
```

- 자동 테스트: `MenuServiceCacheTest`는 Redis를 모의 객체로 대신하므로 Redis 없이 실행됩니다. DB 조회 횟수와 캐시 삭제 시점을 확인합니다.
- 실행: 

```bash
./gradlew test --tests "com.kdt.cafe.menu.MenuServiceCacheTest"    # MacOS·Linux
.\gradlew test --tests "com.kdt.cafe.menu.MenuServiceCacheTest"    # Windows
```

- 확인: 2.3 백엔드 실행 후 `requests/api-check.http`의 C1~C12 실행 (3.2의 정리 요청 뒤)<br>
  `docker exec -it kdt-redis redis-cli --raw`에서 `GET cafe:menus:all`, `TTL cafe:menus:all`로 캐시 저장·삭제를 확인합니다.<br>
  DB 재조회 여부는 앱 콘솔의 `메뉴 캐시 HIT` / `메뉴 캐시 MISS` 로그와 select SQL 로그로 확인합니다.

### 6.4 메뉴 페이징 API

- 엔드포인트: `GET /api/v1/menus/page?page={page}&size={size}`
- 코드
    - `src/main/java/com/kdt/cafe/menu/MenuApiController.java` (`findPage`): page·size 수신 (기본값 0·10)
    - `src/main/java/com/kdt/cafe/menu/MenuService.java` (`findPage`): 범위 검사 후 `PageRequest`로 ID 오름차순 조회
    - `src/main/java/com/kdt/cafe/menu/MenuPageResponse.java`: `content`·`number`·`size`·`totalElements`·`totalPages` 응답
- 동작
    - 판매 중지 메뉴를 포함한 전체 메뉴를 ID 오름차순으로 반환합니다.
    - DB에서 해당 페이지의 데이터만 조회합니다.
    - 범위를 넘는 페이지는 `content: []`와 실제 전체 건수·전체 페이지 수를 반환합니다.
    - 기존 전체 메뉴 목록(A06)의 배열 응답은 그대로 유지합니다.

| 매개변수 | 기본값 | 허용 범위 | 범위 밖·형식 오류 |
|:---|:---:|:---|:---|
| `page` | 0 | 0 이상의 정수 | 400 `VALIDATION_ERROR` |
| `size` | 10 | 1~50의 정수 | 400 `VALIDATION_ERROR` |

- 실행 전: 2.2의 `data.sql` → 3.2의 `api-check.http` → `src/main/resources/pagedata.sql` 순서로 실행 (메뉴 14개)<br>
  `api-check.http`의 A08에서 등록한 5번 메뉴를 A10에서 삭제하므로, 메뉴 ID는 1~15 중 5번이 빠진 상태가 됩니다.<br>
  `api-check.http`를 건너뛰어도 메뉴 개수는 14개로 같아 아래 기대 결과는 동일합니다.
- 확인: 2.3 백엔드 실행 후 `src/page.http` 실행

| 요청 | 기대 결과 |
|:---|:---|
| `page`, `size` 없음 | 200, `number: 0`, `size: 10` |
| `page=0&size=5` | 200, `number: 0`, `size: 5`, `totalElements: 14`, `totalPages: 3` |
| `page=99&size=5` | 200, `content: []`, `number: 99`, `size: 5`, `totalElements: 14`, `totalPages: 3` |
| `page=-1&size=0` | 400 `VALIDATION_ERROR` |
| `page=0&size=0` | 400 `VALIDATION_ERROR` |
| `page=0&size=51` | 400 `VALIDATION_ERROR` |
| `page=abc` | 400 `VALIDATION_ERROR`, `fieldErrors.page` |

### 6.5 미구현 항목

- RestClient 외부 조회는 구현하지 않았습니다. (5절 구현 현황 참고)

## 7. 제출 정보

### 평가 대상

- 브랜치: `main`
- 태그: `submit-v1`
