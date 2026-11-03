package com.kdt.cafe.order;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record CustomerStatisticsResponse(
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,

        String email,
        long orderCount,
        long placedOrderCount,
        long cancelledOrderCount,
        long totalQuantity,
        long totalAmount

) {
}
