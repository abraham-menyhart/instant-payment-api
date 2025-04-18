package com.example.instant_payment_api.controller;

import com.example.instant_payment_api.dto.PaymentRequest;
import com.example.instant_payment_api.dto.TransactionResponse;
import com.example.instant_payment_api.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<TransactionResponse> sendMoney(@RequestBody PaymentRequest request) {
        TransactionResponse transaction = paymentService.processPayment(request);
        return ResponseEntity.ok(transaction);
    }
}
