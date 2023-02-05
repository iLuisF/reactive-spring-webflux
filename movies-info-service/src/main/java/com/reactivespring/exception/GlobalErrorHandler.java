package com.reactivespring.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<String> handlerRequestBodyError(WebExchangeBindException exception) {
        log.error("Exception caught in handleRequestBodyError: {} ", exception.getMessage(), exception);
        String error = exception.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .sorted()
                .collect(Collectors.joining(","));
        log.error("Error is : {} ", error);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

}