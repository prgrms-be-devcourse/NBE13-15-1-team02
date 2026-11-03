package com.kdt.cafe.menu;

import jakarta.validation.constraints.NotNull;

public record MenuAvailableRequest (
        @NotNull Boolean available
){
}
//A21
