package com.kdt.cafe.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MemoUpdateRequest(
        @NotNull(message = "메모를 입력해 주세요.")
        @Size(max = 200, message = "메모는 최대 200자입니다.")
        String memo
) {
}
