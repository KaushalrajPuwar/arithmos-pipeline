package com.arithmos.calc.web;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.arithmos.calc.service.CalculatorService;
import com.arithmos.calc.service.DivisionByZeroException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CalculatorController.class)
class CalculatorControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private CalculatorService calculatorService;

  @Test
  @DisplayName("GET /api/v1/add returns the sum as JSON")
  void addEndpointReturnsSum() throws Exception {
    when(calculatorService.add(2, 3)).thenReturn(5.0);
    mockMvc.perform(get("/api/v1/add").param("a", "2").param("b", "3"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.operation", is("add")))
        .andExpect(jsonPath("$.a", is(2.0)))
        .andExpect(jsonPath("$.b", is(3.0)))
        .andExpect(jsonPath("$.result", is(5.0)));
  }

  @Test
  @DisplayName("GET /api/v1/subtract returns the difference as JSON")
  void subtractEndpointReturnsDifference() throws Exception {
    when(calculatorService.subtract(10, 4)).thenReturn(6.0);
    mockMvc.perform(get("/api/v1/subtract").param("a", "10").param("b", "4"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.operation", is("subtract")))
        .andExpect(jsonPath("$.result", is(6.0)));
  }

  @Test
  @DisplayName("GET /api/v1/multiply returns the product as JSON")
  void multiplyEndpointReturnsProduct() throws Exception {
    when(calculatorService.multiply(3, 7)).thenReturn(21.0);
    mockMvc.perform(get("/api/v1/multiply").param("a", "3").param("b", "7"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.operation", is("multiply")))
        .andExpect(jsonPath("$.result", is(21.0)));
  }

  @Test
  @DisplayName("GET /api/v1/divide returns the quotient as JSON")
  void divideEndpointReturnsQuotient() throws Exception {
    when(calculatorService.divide(20, 4)).thenReturn(5.0);
    mockMvc.perform(get("/api/v1/divide").param("a", "20").param("b", "4"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.operation", is("divide")))
        .andExpect(jsonPath("$.result", is(5.0)));
  }

  @Test
  @DisplayName("GET /api/v1/divide with b=0 returns HTTP 400 with error JSON")
  void divideByZeroReturnsBadRequest() throws Exception {
    when(calculatorService.divide(anyDouble(), anyDouble()))
        .thenThrow(new DivisionByZeroException("Division by zero is not allowed"));
    mockMvc.perform(get("/api/v1/divide").param("a", "5").param("b", "0"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status", is(400)))
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  @DisplayName("missing parameter returns HTTP 400 with error JSON")
  void missingParameterReturnsBadRequest() throws Exception {
    mockMvc.perform(get("/api/v1/add").param("a", "2"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status", is(400)));
  }

  @Test
  @DisplayName("non-numeric parameter returns HTTP 400 with error JSON")
  void nonNumericParameterReturnsBadRequest() throws Exception {
    mockMvc.perform(get("/api/v1/add").param("a", "foo").param("b", "3"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status", is(400)));
  }
}
