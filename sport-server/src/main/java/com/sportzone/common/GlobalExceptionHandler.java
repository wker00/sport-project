package com.sportzone.common;

import com.sportzone.utils.Result;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidException(MethodArgumentNotValidException e) {
        return Result.error(e.getBindingResult().getFieldError().getDefaultMessage());
    }

    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        return Result.error(e.getFieldError().getDefaultMessage());
    }
}