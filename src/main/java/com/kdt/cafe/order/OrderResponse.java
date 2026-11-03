package com.kdt.cafe.order;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

public record OrderResponse(
        Long id,
        String email,
        String address,
        String postcode,

        // 응답 DTO만 OffsetDateTime으로 저장 : +09:00 (엔티티와 DB에는 서울 기준 시각 그대로 저장)
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate shippingDate,

        long totalAmount,
        List<OrderItemResponse> items,
        OrderStatus status,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime cancelledAt,

        String memo
) {
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getEmail(),
                order.getAddress(),
                order.getPostcode(),
                order.getCreatedAt().atZone(SEOUL).toOffsetDateTime(),
                order.getShippingDate(),
                order.getTotalAmount(),
                order.getItems().stream().map(OrderItemResponse::from).toList(),
                order.getStatus(),
                // null.atZone()을 할 경우 NullPointerException 발생 -> null이 아닌 경우에만 호출
                order.getCancelledAt() == null ? null : order.getCancelledAt().atZone(SEOUL).toOffsetDateTime(),
                order.getMemo()
        );
    }
}
