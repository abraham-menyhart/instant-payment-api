package com.example.instant_payment_api.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {
    private Long senderId;
    private Long receiverId;
    private BigDecimal amount;
}
