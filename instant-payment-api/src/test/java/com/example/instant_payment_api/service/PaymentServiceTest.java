package com.example.instant_payment_api.service;

import com.example.instant_payment_api.model.Account;
import com.example.instant_payment_api.model.Transaction;
import com.example.instant_payment_api.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
public class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void testProcessPayment() {
        // Setup accounts
        Account sender = new Account();
        sender.setUsername("Alice");
        sender.setBalance(BigDecimal.valueOf(1000));
        accountRepository.save(sender);

        Account receiver = new Account();
        receiver.setUsername("Bob");
        receiver.setBalance(BigDecimal.valueOf(200));
        accountRepository.save(receiver);

        // Payment
        Transaction tx = paymentService.processPayment(sender.getId(), receiver.getId(), BigDecimal.valueOf(100));

        // Assertions
        assertNotNull(tx.getId());
        Account updatedSender = accountRepository.findById(sender.getId()).get();
        Account updatedReceiver = accountRepository.findById(receiver.getId()).get();

        assertEquals(BigDecimal.valueOf(900), updatedSender.getBalance());
        assertEquals(BigDecimal.valueOf(300), updatedReceiver.getBalance());
    }
}
