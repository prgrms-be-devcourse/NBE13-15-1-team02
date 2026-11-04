package com.kdt.cafe.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionAdvice {

    //@Valid위반
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException ex){
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e->fieldErrors.putIfAbsent(e.getField(),e.getDefaultMessage()));
        ApiError error = new ApiError("VALIDATION_ERROR","입력값을 확인해 주세요.",fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    //@



    //오류 겹치면 400 -> 404 -> 409순서
    @ExceptionHandler(ValidationErrorException.class)
    public ResponseEntity<ApiError> validationErrorExceptionHandler(ValidationErrorException ex){
        ApiError exception = new ApiError("VALIDATION_ERROR", ex.getMessage(), Map.of());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception);
    }

    //404
    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ApiError> categoryNotFoundExceptionHandler(CategoryNotFoundException ex){
        ApiError exception = new ApiError("CATEGORY_NOT_FOUND", ex.getMessage(),Map.of());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception);
    }
    @ExceptionHandler(MenuNotFoundException.class)
    public ResponseEntity<ApiError> menuNotFoundExceptionHandler(MenuNotFoundException ex){
        ApiError exception = new ApiError("MENU_NOT_FOUND", ex.getMessage(),Map.of());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception);
    }
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiError> OrderNotFoundExceptionHandler(OrderNotFoundException ex){
        ApiError exception = new ApiError("ORDER_NOT_FOUND", ex.getMessage(),Map.of());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception);
    }

    //409
    @ExceptionHandler(CategoryNameDuplicatedException.class)
    public ResponseEntity<ApiError> CategoryNameDuplicatedExceptionHandler(CategoryNameDuplicatedException ex){
        ApiError exception = new ApiError("CATEGORY_NAME_DUPLICATED", ex.getMessage(),Map.of());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exception);
    }
    @ExceptionHandler(CategoryInUseException.class)
    public ResponseEntity<ApiError> CategoryInUseExceptionHandler(CategoryInUseException ex){
        ApiError exception = new ApiError("CATEGORY_IN_USE", ex.getMessage(),Map.of());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exception);
    }
    @ExceptionHandler(MenuInUseException.class)
    public ResponseEntity<ApiError> MenuInUseExceptionHandler(MenuInUseException ex){
        ApiError exception = new ApiError("MENU_IN_USE", ex.getMessage(),Map.of());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exception);
    }
    @ExceptionHandler(AddressMissMatchException.class)
    public ResponseEntity<ApiError> AddressMissMatchExceptionHandler(AddressMissMatchException ex){
        ApiError exception = new ApiError("ADDRESS_MISMATCH", ex.getMessage(),Map.of());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exception);
    }
    @ExceptionHandler(OrderNotCancellableException.class)
    public ResponseEntity<ApiError> OrderNotCancellableExceptionHandler(OrderNotCancellableException ex){
        ApiError exception = new ApiError("ORDER_NOT_CANCELLABLE", ex.getMessage(),Map.of());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exception);
    }
    @ExceptionHandler(MenuUnavailableException.class)
    public ResponseEntity<ApiError> MenuUnavailableExceptionHandler(MenuUnavailableException ex){
        ApiError exception = new ApiError("MENU_UNAVAILABLE", ex.getMessage(),Map.of());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exception);
    }



}
