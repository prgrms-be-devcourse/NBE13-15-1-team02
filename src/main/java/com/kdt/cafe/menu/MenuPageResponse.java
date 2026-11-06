package com.kdt.cafe.menu;

import org.springframework.data.domain.Page;

import java.util.List;

public record MenuPageResponse(
        List<MenuResponse> content,
        int number,
        int size,
        Long totalElements,
        int totalPages
) {
    public static MenuPageResponse from(Page<Menu> menus) {
        return new MenuPageResponse(
                menus.stream().map(MenuResponse::from).toList(),
                menus.getNumber(),
                menus.getSize(),
                menus.getTotalElements(),
                menus.getTotalPages()
        );
    }
}
