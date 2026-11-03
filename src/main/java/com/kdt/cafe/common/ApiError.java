package com.kdt.cafe.common;

import java.util.Map;

public record ApiError(String code, String message, Map<String,String>fieldErrors) {
}
