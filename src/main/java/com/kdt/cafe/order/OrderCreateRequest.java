package com.kdt.cafe.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrderCreateRequest(
        @NotNull(message = "이메일을 입력해 주세요.")
        @Size(min = 1, max = 100, message = "이메일은 1~100자여야 합니다.")
        @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+$",
                message = "이메일 형식을 확인해 주세요.")
        String email,

        @NotBlank(message = "주소를 입력해 주세요.")
        @Size(max = 200, message = "주소는 최대 200자입니다.")
        String address,

        @NotNull(message = "우편번호를 입력해 주세요.")
        @Pattern(regexp = "\\d{5}", message = "우편번호는 숫자 5자리입니다.")
        String postcode,

        @NotNull(message = "주문 항목을 입력해 주세요.")
        @Size(min = 1, max = 10, message = "주문 항목은 1~10개여야 합니다.")
                List<@Valid @NotNull OrderItemRequest> items
)
{
    // 주소와 우편번호는 앞뒤 공백 제거 후 검사
    public OrderCreateRequest {
        address = address == null ? null : address.strip();
        postcode = postcode == null ? null : postcode.strip();
    }
}
