package com.kdt.cafe.order;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;

// 선택 심화: 실제 DB 저장·롤백 테스트
// test에 사용할 DB로 kdt-spring-test DB 사용 (앱 실행용 kdt-spring과 분리)
// 클래스에 @Transactional을 붙이지 않음: 붙이면 테스트가 끝날 때 무조건 롤백돼서 OrderService의 실제 커밋·롤백을 구분할 수 없음
@SpringBootTest                 // 앱 전체를 실제 객체로 띄움 (진짜 Service·트랜잭션·DB 연결)
@ActiveProfiles("test")         // application-test.yaml 적용 → 테스트 DB 접속, schema.sql·data.sql 자동 실행
public class OrderDbSaveRollbackTest {

    private static final String ADDRESS = "서울시 강남구 테헤란로 1";     // 주문에 쓰이는 주소 고정
    private static final String POSTCODE = "06236";                   // 주문에 쓰이는 우편번호 고정

    private static final String EMAIL_A = "order-a@test.com";         // ① 정상 주문 A
    private static final String EMAIL_B = "order-b@test.com";         // ② 실패시킬 주문 B
    private static final String EMAIL_C = "order-c@test.com";         // ④ 실패 뒤 정상 주문 C

    private Long firstMenuId;                                         // 주문 첫 번째 항목: Columbia (5000)
    private Long secondMenuId;                                        // 주문 두 번째 항목: Brazil (7000)

    // 실제 OrderService 주입 (모의 객체 아님, 진짜 트랜잭션이 동작)
    @Autowired
    private OrderService orderService;

    // 실제 DB 행을 SQL로 직접 조회, 정리하는 용도
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 진짜 저장을 직접 실행하기 위한 JPA EntityManager (OrderService와 같은 트랜잭션에 참여)
    @PersistenceContext
    private EntityManager entityManager;

    // 진짜 OrderRepository를 감싼 스파이: 평소엔 그대로 동작, ②에서만 save 뒤 예외를 던지게 바꿈
    @MockitoSpyBean
    private OrderRepository orderRepository;

    // 각 테스트 메서드가 실행되기 직전 매번 먼저 실행되는 메서드
    @BeforeEach
    void setUp() {
        // 매 테스트 전 주문, 항목을 비워 항상 같은 상태로 시작 (연속 실행 대비)
        jdbcTemplate.update("DELETE FROM order_item");                // 항목이 주문을 참조하므로 항목 먼저 삭제
        jdbcTemplate.update("DELETE FROM `order`");                   // 그다음 주문 삭제

        // 메뉴 ID는 번호를 가정하지 않고 DB에서 실제 값을 조회
        firstMenuId = jdbcTemplate.queryForObject("SELECT id FROM menu WHERE name = 'Columbia Nariñó'", Long.class);
        secondMenuId = jdbcTemplate.queryForObject("SELECT id FROM menu WHERE name = 'Brazil Serra Do Caparaó'", Long.class);
    }

    // 요구사항: 정상 주문은 실제 DB에 저장되고, 저장 도중 실패한 주문은 주문·항목 모두 취소되며, 실패 뒤에도 정상 주문은 저장된다
    @Test
    void db_save_and_rollback_scenario() {

        // ① 정상 주문 A → DB에서 주문·항목 조회, 이름·단가·수량·금액 확인
        orderService.createOrder(orderRequest(EMAIL_A));              // 진짜 Service 실행 → 커밋
        printState("① A 정상 주문 후");                            // A 커밋 후 DB 상태 출력

        Map<String, Object> orderA = jdbcTemplate.queryForMap(
                "SELECT id, total_amount, status FROM `order` WHERE email = ?", EMAIL_A);  // A의 주문 행 조회
        assertThat(intOf(orderA, "total_amount")).isEqualTo(17000);   // 저장한 A주문 총액이 17000원인지 검증
        assertThat(orderA.get("status")).isEqualTo("PLACED");         // 저장한 A주문 상태가 "PLACED"인지 검증

        List<Map<String, Object>> itemsA = jdbcTemplate.queryForList(
                "SELECT menu_name, unit_price, quantity, line_amount FROM order_item WHERE order_id = ? ORDER BY menu_id",
                orderA.get("id"));                                    // A의 항목 행을 메뉴 ID 순서로 조회
        assertThat(itemsA).hasSize(2);                                // A주문의 주문 항목이 2개인지 검증

        assertThat(itemsA.get(0).get("menu_name")).isEqualTo("Columbia Nariñó");          // A주문 첫번째 주문 항목 메뉴명이 "Columbia Nariñó"인지 검증
        assertThat(intOf(itemsA.get(0), "unit_price")).isEqualTo(5000);            // A주문 첫번째 주문 항목 단가가 5000원인지 검증
        assertThat(intOf(itemsA.get(0), "quantity")).isEqualTo(2);                 // A주문 첫번째 주문 항목 수량이 2개인지 검증
        assertThat(intOf(itemsA.get(0), "line_amount")).isEqualTo(10000);          // A주문 첫번째 주문 항목 금액이 5000 × 2 = 10000인지 검증

        assertThat(itemsA.get(1).get("menu_name")).isEqualTo("Brazil Serra Do Caparaó");  // A주문 두번째 주문 항목 메뉴명이 "Brazil Serra Do Caparaó"인지 검증
        assertThat(intOf(itemsA.get(1), "unit_price")).isEqualTo(7000);            // A주문 두번째 주문 항목 단가가 7000원인지 검증
        assertThat(intOf(itemsA.get(1), "quantity")).isEqualTo(1);                 // A주문 두번째 주문 항목 수량이 1개인지 검증
        assertThat(intOf(itemsA.get(1), "line_amount")).isEqualTo(7000);           // A주문 두번째 주문 항목 금액이 7000 × 1 = 7000인지 검증

        // ② 주문 B 접수 → 저장한 뒤에 실패시키기
        AtomicInteger orderRows = new AtomicInteger();    // 실패 직전 B 주문 행 수 (람다 안에서 값을 바꾸려고 상자에 담음)
        AtomicInteger itemRows = new AtomicInteger();     // 실패 직전 B 항목 행 수
        doAnswer(invocation -> {                          // save가 호출되면 원래 대신 아래 동작을 실행 (invocation = 가로챈 호출 정보)
            Order order = invocation.getArgument(0);      // save에 넘어온 주문 B 꺼내기
            entityManager.persist(order);                 // 진짜 저장 → B 주문·항목 INSERT (cascade로 항목까지, 커밋 전)
            entityManager.flush();                        // 쌓인 INSERT를 지금 바로 DB로 보냄
            printState("② B 저장 직후, 실패 직전(트랜잭션 안)");     // 커밋 전 트랜잭션 안에서 보이는 DB 상태 출력
            orderRows.set(countOrdersByEmail(EMAIL_B));   // 저장 직후 B 주문 행 수 기록 → "저장까지 갔다"는 증거
            itemRows.set(countItemsByEmail(EMAIL_B));     // 저장 직후 B 항목 행 수 기록
            throw new IllegalStateException("저장 후 강제 실패");  // 일부러 예외 → @Transactional이 B 전체를 롤백
        }).when(orderRepository).save(any(Order.class));  // 위 동작을 save(어떤 주문이든) 호출에 예약

        // 진짜 실패했는지: 검증 예외가 아닌 강제 실패 예외가 나왔는지 확인
        assertThatThrownBy(() -> orderService.createOrder(orderRequest(EMAIL_B)))
                .isInstanceOf(IllegalStateException.class);          // B주문 접수 시 강제 실패 예외(IllegalStateException)가 발생하는지 검증

        // 저장한 뒤에 실패했는지: 실패 직전엔 B의 주문·항목이 있었음 (저장 전 실패와 구분)
        assertThat(orderRows.get()).isEqualTo(1);                  // 실패 직전 B주문 행이 1건 있었는지 검증
        assertThat(itemRows.get()).isGreaterThanOrEqualTo(1);        // 실패 직전 B주문 항목 행이 1개 이상 있었는지 검증

        // ③ 다 취소됐는지: 있던 B가 사라지고 A만 남음 = 롤백 성공
        printState("③ B 롤백 후");                                       // 롤백 후 DB 상태 출력
        assertThat(countOrders()).isEqualTo(1);                      // 전체 주문이 A 1건뿐인지 검증
        assertThat(countItems()).isEqualTo(2);                       // 전체 주문 항목이 A의 2개뿐인지 검증
        assertThat(countOrdersByEmail(EMAIL_A)).isEqualTo(1);        // A주문이 그대로 1건 남아 있는지 검증
        assertThat(countOrdersByEmail(EMAIL_B)).isZero();                    // B주문 행이 0건인지 검증
        assertThat(countItemsByEmail(EMAIL_B)).isZero();                     // B주문 항목 행이 0개인지 검증

        // ④ 정상 주문 C → DB에 A, C 있음
        reset(orderRepository);                                              // 스파이를 원래 동작으로 되돌림
        orderService.createOrder(orderRequest(EMAIL_C));                     // 실패 뒤 같은 Service로 다시 주문
        printState("④ C 정상 주문 후");                                  // C 커밋 후 DB 상태 출력

        assertThat(countOrders()).isEqualTo(2);                      // 전체 주문이 A, C 2건인지 검증
        assertThat(countItems()).isEqualTo(4);                       // 전체 주문 항목이 A 2개 + C 2개 = 4개인지 검증
        assertThat(countOrdersByEmail(EMAIL_A)).isEqualTo(1);        // A주문이 그대로 1건 남아 있는지 검증
        assertThat(countOrdersByEmail(EMAIL_C)).isEqualTo(1);        // C주문이 1건 저장됐는지 검증
        assertThat(countOrdersByEmail(EMAIL_B)).isZero();                    // B주문이 여전히 0건인지 검증

    }

    // 주문 요청 만들기
    // 이메일이 주문을 구분하는 기준이기 때문에 email만 다르게 받음
    // 주소, 우편번호, 메뉴 2개가 채워진 주문 요청을 만들어주는 메서드, 총액은 항상 17,000
    private OrderCreateRequest orderRequest(String email) {
        return new OrderCreateRequest(email, ADDRESS, POSTCODE, List.of(
                new OrderItemRequest(firstMenuId, 2),                 // Columbia 2개 → 10000
                new OrderItemRequest(secondMenuId, 1)                 // Brazil 1개 → 7000
        ));
    }

    // [추가] 현재 DB 상태를 콘솔에 한 줄로 출력 (단계별 검증 결과를 눈으로 확인하는 용도)
    private void printState(String step) {
        System.out.printf("[%s] 전체 주문 %d건, 전체 항목 %d개 | A=%d, B=%d, C=%d%n",
                step, countOrders(), countItems(),
                countOrdersByEmail(EMAIL_A), countOrdersByEmail(EMAIL_B), countOrdersByEmail(EMAIL_C));
    }

    // 전체 주문 수 - order 테이블의 행 개수
    private int countOrders() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM `order`", Integer.class);
    }

    // 전체 주문 항목 수 - order_item 테이블의 행 개수
    private int countItems() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM order_item", Integer.class);
    }

    // 특정 이메일의 주문 수 - 롤백된 주문은 ID가 남지 않으므로 이메일로 존재 여부 확인
    private int countOrdersByEmail(String email) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM `order` WHERE email = ?", Integer.class, email);
    }

    // 특정 이메일 주문의 항목 수 - 주문과 항목을 이어서 해당 주문에 딸린 항목만 셈
    private int countItemsByEmail(String email) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM order_item oi JOIN `order` o ON oi.order_id = o.id WHERE o.email = ?",
                Integer.class, email);
    }

    // 숫자 컬럼을 int로 꺼냄 (드라이버에 따라 Integer·Long으로 오므로 Number로 받아 통일)
    private int intOf(Map<String, Object> row, String column) {
        return ((Number) row.get(column)).intValue();
    }

}