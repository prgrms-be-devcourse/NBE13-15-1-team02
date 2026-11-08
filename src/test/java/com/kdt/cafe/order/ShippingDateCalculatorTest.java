package com.kdt.cafe.order;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

// 업무 규칙 자동 테스트 : 배송일 계산과 취소 가능 여부 (서울 시각 14:00:00 기준)
// ShippingDateCalculator는 시각을 매개변수로 받으므로 고정 시각을 넣어 검증
public class ShippingDateCalculatorTest {

    @Test
    // 접수일이 13:59:59인 주문은 당일 배송
    void received_at_135959_ships_same_day() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 11, 3, 13, 59, 59);

        assertEquals(LocalDate.of(2026, 11, 3), ShippingDateCalculator.shippingDateOf(createdAt));
    }

    @Test
    // 접수일이 14:00:00인 주문은 익일 배송
    void received_at_140000_ships_next_day() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 11, 3, 14, 0, 0);

        assertEquals(LocalDate.of(2026, 11, 4), ShippingDateCalculator.shippingDateOf(createdAt));
    }

    @Test
    // 접수일이 14:00:01인 주문은 익일 배송 (연말에는 다음 해로 넘어감)
    void received_on_dec31_at_140001_ships_next_year() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 12, 31, 14, 0, 1);

        assertEquals(LocalDate.of(2027, 1, 1), ShippingDateCalculator.shippingDateOf(createdAt));
    }

    @Test
    // 배송일 13:59:59에는 주문 취소 가능
    void cancellable_at_135959_on_shipping_date() {
        LocalDate shippingDate = LocalDate.of(2026, 11, 3);

        assertTrue(ShippingDateCalculator.isCancellable(shippingDate, LocalDateTime.of(2026,11,3,13,59,59)));
    }

    @Test
    // 배송일 14:00:00에는 주문 취소 불가
    void not_cancellable_at_140000_on_shipping_date() {
        LocalDate shippingDate = LocalDate.of(2026,11,3);

        assertFalse(ShippingDateCalculator.isCancellable(shippingDate, LocalDateTime.of(2026,11,3,14,0,0)));
    }
}
