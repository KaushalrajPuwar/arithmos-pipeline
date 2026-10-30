package com.arithmos.calc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CalculatorServiceTest {

  private final CalculatorService calculatorService = new CalculatorService();

  @Test
  @DisplayName("add returns the sum of two numbers")
  void addReturnsSum() {
    assertEquals(5.0, calculatorService.add(2, 3));
  }

  @Test
  @DisplayName("add handles negative numbers")
  void addHandlesNegatives() {
    assertEquals(-1.0, calculatorService.add(2, -3));
  }

  @Test
  @DisplayName("subtract returns the difference of two numbers")
  void subtractReturnsDifference() {
    assertEquals(1.0, calculatorService.subtract(4, 3));
  }

  @Test
  @DisplayName("multiply returns the product of two numbers")
  void multiplyReturnsProduct() {
    assertEquals(12.0, calculatorService.multiply(3, 4));
  }

  @Test
  @DisplayName("multiply by zero returns zero")
  void multiplyByZeroReturnsZero() {
    assertEquals(0.0, calculatorService.multiply(9, 0));
  }

  @Test
  @DisplayName("divide returns the quotient of two numbers")
  void divideReturnsQuotient() {
    assertEquals(2.5, calculatorService.divide(5, 2));
  }

  @Test
  @DisplayName("divide by zero throws DivisionByZeroException")
  void divideByZeroThrows() {
    assertThrows(DivisionByZeroException.class, () -> calculatorService.divide(5, 0));
  }
}
