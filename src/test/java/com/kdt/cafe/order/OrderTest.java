package com.kdt.cafe.order;

import com.kdt.cafe.category.Category;
import com.kdt.cafe.common.OrderNotCancellableException;
import com.kdt.cafe.menu.Menu;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

// 업무 규칙 자동 테스트 : 주문 취소 마감과 재취소 (실제 Order 엔티티의 cancel 로직 실행)
// Order는 접수 시각과 취소 시각을 매개변수로 받으므로 고정 시각을 넣어 경계값을 검증
public class OrderTest {

    // 2026-11-03 10:00:00 접수 -> 배송일 : 2026-11-03 / 취소 마감 : 2026-11-03 14:00:00
    private Order newOrder() {
        Menu menu = new Menu(new Category("싱글오리진"), "Columbia Nariñó", 5000, "콜롬비아 나리뇨 원두");
        Order order = new Order("buyer@example.com", "서울시 강남구 테헤란로 1", "12345", LocalDateTime.of(2026,11,3,10,0,0));
        order.addItem(menu, 2);
        return order;
    }

    @Test
    // 배송일 13:59:59에는 주문 취소 성공 (상태와 취소 시각만 변경됨)
    void cancel_at_135959_succeeds() {
        // 준비
        Order order = newOrder();
        LocalDateTime now = LocalDateTime.of(2026,11,3,13,59,59);

        // 실행
        order.cancel(now);

        // 검증
        assertEquals(LocalDate.of(2026,11,3), order.getShippingDate());
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(now, order.getCancelledAt());
        assertEquals(10000, order.getTotalAmount());
        assertEquals(1, order.getItems().size());
    }

    @Test
    // 배송일 14:00:00에는 주문 취소 불가 -> 예외 -> 주문은 변경 사항이 없어야 함
    void cancel_at_140000_fails() {
        // 준비
        Order order = newOrder();

        // 실행 + 검증
        OrderNotCancellableException error = assertThrows(OrderNotCancellableException.class,
                () -> order.cancel(LocalDateTime.of(2026,11,3,14,0,0)));
        assertEquals("배송 대상일 14시 전의 접수 상태 주문만 취소할 수 있습니다.", error.getMessage());
        assertEquals(OrderStatus.PLACED, order.getStatus());
        assertNull(order.getCancelledAt());
    }

    @Test
    // 이미 취소된 주문은 취소 마감 이후에 재취소 해도 성공 (최초 취소 시각과 기존 주문 값 유지)
    void recancel_after_deadline_keeps_first_cancelled_at() {
        // 준비
        Order order = newOrder();
        LocalDateTime firstCancelledAt = LocalDateTime.of(2026,11,3,13,0,0);
        order.cancel(firstCancelledAt);

        // 실행 : 16:00:00(취소 마감 시간 이후)에 재취소 -> 예외 없음 (200)
        order.cancel(LocalDateTime.of(2026,11,3,16,0,0));

        // 검증
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(firstCancelledAt, order.getCancelledAt());
        assertEquals(10000, order.getTotalAmount());
        assertEquals(1, order.getItems().size());
        assertEquals(10000, order.getItems().get(0).getLineAmount());
    }
}
