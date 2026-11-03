package com.kdt.cafe.common;

public class MenuUnavailableException extends RuntimeException {
    public MenuUnavailableException(String message) {
        super(message);
    }
}
