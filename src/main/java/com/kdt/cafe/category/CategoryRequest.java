package com.kdt.cafe.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        // null, "", 공백만 있는 값 거부, 글자수 최대 30자
        @NotBlank @Size(max = 30) String name
) {

    // 앞뒤 공백·탭·줄바꿈 제거, 중간 공백은 유지
    public String trimmedName() {
        return name.trim();
    }

}
