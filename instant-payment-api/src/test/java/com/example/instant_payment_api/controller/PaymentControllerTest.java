package com.example.instant_payment_api.controller;

import com.example.instant_payment_api.dto.PaymentRequest;
import com.example.instant_payment_api.dto.TransactionResponse;
import com.example.instant_payment_api.exception.InsufficientBalanceException;
import com.example.instant_payment_api.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturn200AndTransactionResponse() throws Exception {
        // Given
        TransactionResponse mockResponse = TransactionResponse.builder()
                .id(123L)
                .senderAccountId(1L)
                .receiverAccountId(2L)
                .amount(BigDecimal.valueOf(100.00))
                .build();

        when(paymentService.processPayment(any(PaymentRequest.class)))
                .thenReturn(mockResponse);

        PaymentRequest request = new PaymentRequest();
        request.setSenderId(1L);
        request.setReceiverId(2L);
        request.setAmount(BigDecimal.valueOf(100.00));

        // When + Then
        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(123))
                .andExpect(jsonPath("$.senderAccountId").value(1))
                .andExpect(jsonPath("$.receiverAccountId").value(2))
                .andExpect(jsonPath("$.amount").value(100.00));
    }

    @Test
    void shouldReturn400WhenInsufficientBalance() throws Exception {
        // Given
        when(paymentService.processPayment(any(PaymentRequest.class)))
                .thenThrow(new InsufficientBalanceException("Insufficient balance in sender's account."));

        PaymentRequest request = new PaymentRequest();
        request.setSenderId(1L);
        request.setReceiverId(2L);
        request.setAmount(BigDecimal.valueOf(99999999));

        // When + Then
        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Insufficient balance in sender's account."));
    }
}
