package com.example.instant_payment_api.service;

import com.example.instant_payment_api.dto.TransactionResponse;
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
    public TransactionResponse processPayment(Long senderId, Long receiverId, BigDecimal amount) {
        Account sender = getAccountForUpdate(senderId, "Sender account not found.");
        Account receiver = getAccountForUpdate(receiverId, "Receiver account not found.");

        checkSufficientBalance(sender, amount);

        updateBalances(sender, receiver, amount);

        accountRepository.save(sender);
        accountRepository.save(receiver);
        Transaction transaction = saveTransactionLog(senderId, receiverId, amount);

        publishNotification(senderId, receiverId, amount);

        return TransactionResponse.builder()
                .id(transaction.getId())
                .senderAccountId(transaction.getSenderAccountId())
                .receiverAccountId(transaction.getReceiverAccountId())
                .amount(transaction.getAmount())
                .timestamp(transaction.getTimestamp())
                .build();
    }

    private Account getAccountForUpdate(Long accountId, String errorMessage) {
        return accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new RuntimeException(errorMessage));
    }

    private void checkSufficientBalance(Account sender, BigDecimal amount) {
        if (sender.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance in sender's account.");
        }
    }

    private void updateBalances(Account sender, Account receiver, BigDecimal amount) {
        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));
    }

    private Transaction saveTransactionLog(Long senderId, Long receiverId, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setSenderAccountId(senderId);
        transaction.setReceiverAccountId(receiverId);
        transaction.setAmount(amount);
        transaction.setTimestamp(LocalDateTime.now());
        return transactionRepository.save(transaction);
    }

    private void publishNotification(Long senderId, Long receiverId, BigDecimal amount) {
        String notification = String.format(
                "Transaction from %d to %d for amount %s completed.",
                senderId, receiverId, amount
        );
        kafkaTemplate.send("transaction-notifications", notification);
    }
}
