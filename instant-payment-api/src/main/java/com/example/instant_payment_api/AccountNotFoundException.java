package com.example.instant_payment_api;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(Long accountId) {
        super("Account not found with ID: " + accountId);
    }

    public AccountNotFoundException(String message) {
        super(message);
    }
}
