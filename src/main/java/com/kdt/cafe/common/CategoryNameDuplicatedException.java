package com.kdt.cafe.common;

public class CategoryNameDuplicatedException extends RuntimeException {
    public CategoryNameDuplicatedException(String message) {
        super(message);
    }
}
