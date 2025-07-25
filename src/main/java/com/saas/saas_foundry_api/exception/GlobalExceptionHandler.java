package com.saas.saas_foundry_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import jakarta.ws.rs.ForbiddenException;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<String> handleDuplicateResourceException(DuplicateResourceException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<String> handleResourceNotFoundException(ResourceNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<String> handleForbiddenException(ForbiddenException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
  }
}
