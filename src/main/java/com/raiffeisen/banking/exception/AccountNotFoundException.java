package com.raiffeisen.banking.exception;

public class AccountNotFoundException extends RuntimeException {
    private static final String MSG = "There is no Account with ID = %s in database";

    public AccountNotFoundException(Integer accountId) {
        super(MSG.formatted(accountId));
    }
}
