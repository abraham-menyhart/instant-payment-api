package com.example.instant_payment_api.service;

import com.example.instant_payment_api.model.Account;
import com.example.instant_payment_api.model.Transaction;
import com.example.instant_payment_api.repository.AccountRepository;
import com.example.instant_payment_api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Transactional
    public Transaction processPayment(Long senderId, Long receiverId, BigDecimal amount) {
        // Lock accounts to prevent race conditions
        Account sender = accountRepository.findByIdForUpdate(senderId)
                .orElseThrow(() -> new RuntimeException("Sender account not found."));
        Account receiver = accountRepository.findByIdForUpdate(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver account not found."));

        // Check balance
        if (sender.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance in sender's account.");
        }

        // Deduct from sender, add to receiver
        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));

        // Save updates
        accountRepository.save(sender);
        accountRepository.save(receiver);

        // Save transaction log
        Transaction transaction = new Transaction();
        transaction.setSenderAccountId(senderId);
        transaction.setReceiverAccountId(receiverId);
        transaction.setAmount(amount);
        transaction.setTimestamp(LocalDateTime.now());
        transaction = transactionRepository.save(transaction);

        // Publish notification event to Kafka
        String notification = "Transaction from " + senderId + " to " + receiverId
                + " for amount " + amount + " completed.";
        kafkaTemplate.send("transaction-notifications", notification);

        return transaction;
    }
}

