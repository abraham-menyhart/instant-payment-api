package com.example.instant_payment_api.controller;

import com.example.instant_payment_api.model.Transaction;
import com.example.instant_payment_api.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Transaction> sendMoney(@RequestParam Long senderId,
                                                 @RequestParam Long receiverId,
                                                 @RequestParam BigDecimal amount) {
        try {
            Transaction transaction = paymentService.processPayment(senderId, receiverId, amount);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            // Provide proper error handling in real code
            return ResponseEntity.status(BAD_REQUEST).build();
        }
    }
}
