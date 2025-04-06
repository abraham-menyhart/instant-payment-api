package com.example.instant_payment_api.converter;

import com.example.instant_payment_api.dto.TransactionResponse;
import com.example.instant_payment_api.model.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionResponseConverter {

    public TransactionResponse convert(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .senderAccountId(transaction.getSenderAccountId())
                .receiverAccountId(transaction.getReceiverAccountId())
                .amount(transaction.getAmount())
                .timestamp(transaction.getTimestamp())
                .build();
    }
}
