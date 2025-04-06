package com.example.instant_payment_api.service;

import com.example.instant_payment_api.AccountNotFoundException;
import com.example.instant_payment_api.converter.TransactionResponseConverter;
import com.example.instant_payment_api.dto.PaymentRequest;
import com.example.instant_payment_api.dto.TransactionResponse;
import com.example.instant_payment_api.exception.InsufficientBalanceException;
import com.example.instant_payment_api.model.Account;
import com.example.instant_payment_api.model.Transaction;
import com.example.instant_payment_api.repository.AccountRepository;
import com.example.instant_payment_api.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionResponseConverter converter;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private PaymentService paymentService;


    @Test
    void shouldProcessPaymentSuccessfully() {
        // Given
        Long senderId = 1L;
        Long receiverId = 2L;
        BigDecimal amount = new BigDecimal("100.00");

        Account sender = new Account();
        sender.setId(senderId);
        sender.setBalance(new BigDecimal("500.00"));

        Account receiver = new Account();
        receiver.setId(receiverId);
        receiver.setBalance(new BigDecimal("200.00"));

        when(accountRepository.findByIdForUpdate(senderId)).thenReturn(Optional.of(sender));
        when(accountRepository.findByIdForUpdate(receiverId)).thenReturn(Optional.of(receiver));

        Transaction transaction = new Transaction();
        transaction.setId(123L);
        transaction.setSenderAccountId(senderId);
        transaction.setReceiverAccountId(receiverId);
        transaction.setAmount(amount);
        transaction.setTimestamp(LocalDateTime.now());

        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionResponse expectedResponse = TransactionResponse.builder()
                .id(123L)
                .senderAccountId(senderId)
                .receiverAccountId(receiverId)
                .amount(amount)
                .timestamp(transaction.getTimestamp())
                .build();

        when(converter.convert(transaction)).thenReturn(expectedResponse);

        PaymentRequest request = new PaymentRequest();
        request.setSenderId(senderId);
        request.setReceiverId(receiverId);
        request.setAmount(amount);

        // When
        TransactionResponse response = paymentService.processPayment(request);

        // Then
        assertEquals(expectedResponse, response);
        assertEquals(new BigDecimal("400.00"), sender.getBalance());
        assertEquals(new BigDecimal("300.00"), receiver.getBalance());

        verify(accountRepository).saveAll(of(sender, receiver));
        verify(transactionRepository).save(any(Transaction.class));
        verify(kafkaTemplate).send(eq("transaction-notifications"), contains("Transaction from 1 to 2"));
    }

    @Test
    void shouldThrowExceptionWhenSenderAccountNotFound() {
        //Given
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.empty());

        PaymentRequest request = new PaymentRequest();
        request.setSenderId(1L);
        request.setReceiverId(2L);
        request.setAmount(BigDecimal.TEN);

        //When
        AccountNotFoundException ex = assertThrows(AccountNotFoundException.class, () -> paymentService.processPayment(request));

        //Then
        assertEquals("Account not found with ID: 1", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenReceiverAccountNotFound() {
        //Given
        Account sender = new Account();
        sender.setId(1L);
        sender.setBalance(new BigDecimal("100.00"));

        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(sender));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.empty());

        PaymentRequest request = new PaymentRequest();
        request.setSenderId(1L);
        request.setReceiverId(2L);
        request.setAmount(BigDecimal.TEN);

        //When
        AccountNotFoundException ex = assertThrows(AccountNotFoundException.class, () -> paymentService.processPayment(request));

        //Then
        assertEquals("Account not found with ID: 2", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenInsufficientBalance() {
        //Given
        Account sender = new Account();
        sender.setId(1L);
        sender.setBalance(new BigDecimal("5.00"));

        Account receiver = new Account();
        receiver.setId(2L);
        receiver.setBalance(new BigDecimal("0.00"));

        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(sender));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(receiver));

        PaymentRequest request = new PaymentRequest();
        request.setSenderId(1L);
        request.setReceiverId(2L);
        request.setAmount(new BigDecimal("10.00"));

        //When
        InsufficientBalanceException ex = assertThrows(InsufficientBalanceException.class, () -> paymentService.processPayment(request));

        //Then
        assertEquals("Insufficient balance in sender's account.", ex.getMessage());
    }
}
