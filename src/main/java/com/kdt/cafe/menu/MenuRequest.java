package com.kdt.cafe.menu;

import jakarta.validation.constraints.*;

public record MenuRequest(
    @NotNull @Positive Long categoryId,
    @NotNull @Size(min = 1, max = 100) String name,
    @NotNull @Min(1) @Max(1_000_000) Integer price,
    @NotNull @Size(max = 300) String description
) {
    public MenuRequest {
        name = (name == null) ? null : name.trim();
        description = (description == null) ? null : description.trim();
    }
}

//A08등록, A09 수정