package com.bankname.banking.exception;

public class AccountNotCreatedException extends RuntimeException {
    public AccountNotCreatedException(String message) {
        super(message);
    }
}
