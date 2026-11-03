package com.kdt.cafe.common;

public class MenuInUseException extends RuntimeException {
    public MenuInUseException(String message) {
        super(message);
    }
}
