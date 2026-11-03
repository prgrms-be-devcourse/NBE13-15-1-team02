package com.kdt.cafe.order;

public record OrderItemResponse(
        Long menuId,
        String menuName,
        long unitPrice,
        int quantity,
        long lineAmount
) {
    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getMenu().getId(),
                item.getMenuName(),     // 주문 당시 메뉴 이름
                item.getUnitPrice(),    // 주문 당시 가격
                item.getQuantity(),
                item.getLineAmount()
        );
    }
}
