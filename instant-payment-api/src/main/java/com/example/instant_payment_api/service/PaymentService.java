package com.example.instant_payment_api.service;

import com.example.instant_payment_api.converter.TransactionResponseConverter;
import com.example.instant_payment_api.dto.PaymentRequest;
import com.example.instant_payment_api.dto.TransactionResponse;
import com.example.instant_payment_api.exception.AccountNotFoundException;
import com.example.instant_payment_api.exception.InsufficientBalanceException;
import com.example.instant_payment_api.model.Account;
import com.example.instant_payment_api.model.Transaction;
import com.example.instant_payment_api.repository.AccountRepository;
import com.example.instant_payment_api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String TRANSACTION_TOPIC = "transaction-notifications";

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionResponseConverter transactionResponseConverter;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Transactional
    public TransactionResponse processPayment(PaymentRequest request) {
        log.info("Processing payment: {}", request);

        Account sender = getAccountForUpdate(request.getSenderId());
        Account receiver = getAccountForUpdate(request.getReceiverId());

        checkSufficientBalance(sender, request.getAmount());

        executeTransfer(sender, receiver, request.getAmount());

        Transaction transaction = logTransaction(request.getSenderId(), request.getReceiverId(), request.getAmount());

        notifyParties(transaction);

        log.info("Payment processed successfully. Transaction ID: {}", transaction.getId());

        return transactionResponseConverter.convert(transaction);
    }

    private Account getAccountForUpdate(Long accountId) {
        return accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> {
                    log.warn("Account not found with ID: {}", accountId);
                    return new AccountNotFoundException(accountId);
                });
    }

    private void checkSufficientBalance(Account sender, BigDecimal amount) {
        if (sender.getBalance().compareTo(amount) < 0) {
            log.warn("Insufficient balance for account ID {}. Available: {}, Required: {}",
                    sender.getId(), sender.getBalance(), amount);
            throw new InsufficientBalanceException("Insufficient balance in sender's account.");
        }
    }

    private void executeTransfer(Account sender, Account receiver, BigDecimal amount) {
        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));
        accountRepository.saveAll(List.of(sender, receiver));
        log.debug("Balances updated. Sender ID: {}, Receiver ID: {}, Amount: {}", sender.getId(), receiver.getId(), amount);
    }

    private Transaction logTransaction(Long senderId, Long receiverId, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setSenderAccountId(senderId);
        transaction.setReceiverAccountId(receiverId);
        transaction.setAmount(amount);
        transaction.setTimestamp(LocalDateTime.now());
        Transaction saved = transactionRepository.save(transaction);
        log.debug("Transaction logged: {}", saved);
        return saved;
    }

    private void notifyParties(Transaction transaction) {
        String notification = String.format(
                "Transaction from %d to %d for amount %s completed.",
                transaction.getSenderAccountId(),
                transaction.getReceiverAccountId(),
                transaction.getAmount()
        );
        kafkaTemplate.send(TRANSACTION_TOPIC, notification);
        log.debug("Notification sent to Kafka: {}", notification);
    }
}
