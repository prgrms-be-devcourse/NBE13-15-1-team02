package com.kdt.cafe.common;

import java.util.Map;

public class ValidationErrorException extends RuntimeException {

    private Map<String,String>fieldError;

    public ValidationErrorException(String message, Map<String ,String> fieldError) {
        super(message);
        this.fieldError=fieldError;
    }

//    public ValidationErrorException(String message) {
//        super(message);
//    }

    public Map<String,String> getFieldErrors(){
        return fieldError;
    }

}
