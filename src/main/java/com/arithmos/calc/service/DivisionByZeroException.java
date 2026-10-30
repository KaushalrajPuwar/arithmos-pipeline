package com.arithmos.calc.service;

public class DivisionByZeroException extends RuntimeException {

  public DivisionByZeroException(String message) {
    super(message);
  }
}
