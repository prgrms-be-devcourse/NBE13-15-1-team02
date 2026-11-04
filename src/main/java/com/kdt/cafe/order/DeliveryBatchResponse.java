package com.kdt.cafe.order;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

public record DeliveryBatchResponse(
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate shippingDate,

        String email,
        String address,
        String postcode,
        List<Long> orderIds,
        long orderCount,
        long totalQuantity,
        long totalAmount,
        List<OrderItemResponse> items
) {
}
