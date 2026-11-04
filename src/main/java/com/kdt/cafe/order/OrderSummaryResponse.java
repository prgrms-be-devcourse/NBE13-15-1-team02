package com.kdt.cafe.order;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

//이메일별 주문 목록
public record OrderSummaryResponse(
        Long id,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate shippingDate,

        long totalAmount,
        OrderStatus status
) {
    public static OrderSummaryResponse from(Order order) {
        return new OrderSummaryResponse(
                order.getId(),
                order.getCreatedAt().atZone(ZoneId.of("Asia/Seoul")).toOffsetDateTime(),
                order.getShippingDate(),
                order.getTotalAmount(),
                order.getStatus()
        );
    }
}
