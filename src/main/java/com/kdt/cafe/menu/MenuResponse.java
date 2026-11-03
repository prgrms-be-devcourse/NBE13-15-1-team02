package com.kdt.cafe.menu;

public record MenuResponse(
        Long id,
        Long categoryId,
        String name,
        int price,
        String description,
        boolean available
){
    public static MenuResponse from(Menu menu) {
        return new MenuResponse(
                menu.getId(),
                menu.getCategory().getId(),
                menu.getName(),
                menu.getPrice(),
                menu.getDescription(),
                menu.isAvailable()
        );
    }
}
