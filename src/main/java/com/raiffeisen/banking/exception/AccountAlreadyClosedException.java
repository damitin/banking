package com.raiffeisen.banking.exception;

public class AccountAlreadyClosedException extends RuntimeException {
    private static final String MSG = "Account with ID = %d is already closed";

    public AccountAlreadyClosedException(Integer accountId) {
        super(MSG.formatted(accountId));
    }
}
