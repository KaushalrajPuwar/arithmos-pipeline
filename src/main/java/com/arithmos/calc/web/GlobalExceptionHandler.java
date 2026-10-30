package com.arithmos.calc.web;

import com.arithmos.calc.service.DivisionByZeroException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(DivisionByZeroException.class)
  public ResponseEntity<ApiError> handleDivisionByZero(
      DivisionByZeroException ex, HttpServletRequest request) {
    return error(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
  }

  @ExceptionHandler({
      MissingServletRequestParameterException.class,
      MethodArgumentTypeMismatchException.class
  })
  public ResponseEntity<ApiError> handleBadInput(Exception ex, HttpServletRequest request) {
    return error(HttpStatus.BAD_REQUEST, "Parameters 'a' and 'b' must be valid numbers", request);
  }

  private ResponseEntity<ApiError> error(
      HttpStatus status, String message, HttpServletRequest request) {
    ApiError body = new ApiError(
        Instant.now(), status.value(), status.getReasonPhrase(), message, request.getRequestURI());
    return ResponseEntity.status(status).body(body);
  }
}
