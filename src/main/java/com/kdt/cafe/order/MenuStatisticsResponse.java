package com.kdt.cafe.order;

public record MenuStatisticsResponse(
        Long menuId,
        String menuName,
        int unitPrice,
        long totalQuantity,
        long totalAmount
) {
}
