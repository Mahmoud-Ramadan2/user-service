package com.mahmoud.appointmentsystem.user_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

@ExceptionHandler(UserNotFoundException.class)
public ResponseEntity<String> userNotFoundExceptionHandler(UserNotFoundException ex){

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
}

}
