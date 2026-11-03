package com.kdt.cafe.category;

/**
 * 카테고리 응답 DTO
 * */
public record CategoryResponse(
        Long id,
        String name) {

    // 엔티티 -> 응답 DTO 변환
    // Service에서 category.stream().map(CategoryResponse::toResponse) 형태로 사용
    public static CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }



}
