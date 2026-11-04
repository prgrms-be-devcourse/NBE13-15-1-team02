package com.kdt.cafe.order;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

// 배송일 계산 & 취소 가능 여부 판단 (취소 마감 : 서울 시간 14시 기준)
// 유틸리티 클래스 : 상속용이 아님
public final class ShippingDateCalculator {

    // 기준 시각 : 14시
    private static final LocalTime CUTOFF = LocalTime.of(14, 0);

    // 생성자는 막아 둠 (객체를 만들 필요가 없는 클래스)
    private ShippingDateCalculator() {}

    // 접수 시각 기준으로 배송일 계산 (14시 이후부터 다음 날 배송)
    public static LocalDate shippingDateOf(LocalDateTime createdAt) {
        LocalDate date = createdAt.toLocalDate();
        if (createdAt.toLocalTime().isBefore(CUTOFF)) {
            return date;
        }
        return date.plusDays(1);
    }

    // 배송일 14시 기준으로 취소 가능 여부 판단
    public static boolean isCancellable(LocalDate shippingDate, LocalDateTime now) {
        LocalDateTime deadline = shippingDate.atTime(CUTOFF);
        return now.isBefore(deadline);
    }

}
