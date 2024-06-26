package com.raiffeisen.banking.exception;

public class AccountCanNotBeClosedException extends RuntimeException {
    private static final String MSG = "Account with ID = %d cannot be closed because balance is less than 0";
    public AccountCanNotBeClosedException(Integer accountId) {
        super(MSG.formatted(accountId));
    }
}
