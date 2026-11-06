package com.kdt.cafe.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionAdvice {

    //@Valid위반
    //400 badRequest
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException ex){
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e->fieldErrors.putIfAbsent(e.getField(),e.getDefaultMessage()));
        ApiError error = new ApiError("VALIDATION_ERROR","입력값을 확인해 주세요.",fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    //JSON 문법오류 httpMessageNotReadableException.class
    //400 badRequest
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> theOtherBadRequestExceptionHandler(HttpMessageNotReadableException ex){
        Map<String,String> fieldError = new HashMap<>();
        ApiError error = new ApiError("VALIDATION_ERROR","입력값을 확인해 주세요.",fieldError);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    //누락
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> MissingServletRequestParameterExceptionHandler(MissingServletRequestParameterException ex){
        Map<String,String> fieldError = new HashMap<>();
        fieldError.put(ex.getParameterName(),"필수 값입니다.");
        ApiError error = new ApiError("VALIDATION_ERROR",
                "입력값을 확인해 주세요.",fieldError);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    //타입 불일치 (예: /menus/abc, ?shippingDate=abc)
    //400 badRequest
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> methodArgumentTypeMismatchExceptionHandler(MethodArgumentTypeMismatchException ex){
        Map<String,String> fieldError = new HashMap<>();
        // getName() : 컨트롤러에 적은 이름 (menuId, orderId, shippingDate ...)
        fieldError.put(ex.getName(), "형식이 올바르지 않습니다.");
        ApiError error = new ApiError("VALIDATION_ERROR",
                "입력값을 확인해 주세요.",fieldError);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }



    //오류 겹치면 400 -> 404 -> 409순서
    @ExceptionHandler(ValidationErrorException.class)
    public ResponseEntity<ApiError> validationErrorExceptionHandler(ValidationErrorException ex){
        ApiError exception = new ApiError("VALIDATION_ERROR", ex.getMessage(),ex.getFieldErrors());
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
