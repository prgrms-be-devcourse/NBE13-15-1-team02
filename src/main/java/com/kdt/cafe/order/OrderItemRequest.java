package com.kdt.cafe.order;

import jakarta.validation.constraints.*;

public record OrderItemRequest(
        @NotNull(message = "메뉴 ID를 입력해 주세요.")
        @Positive(message = "메뉴 ID는 1 이상이어야 합니다.")
        Long menuId,

        @NotNull(message = "수량을 입력해 주세요.")
        @Min(value = 1, message = "수량은 1~20개여야 합니다.")
        @Max(value = 20, message = "수량은 1~20개여야 합니다.")
        Integer quantity
) {
}
