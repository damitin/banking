package com.raiffeisen.banking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AccountNotFoundException extends RuntimeException {
    private static final String MSG = "There is no Account with ID = %s in database";

    public AccountNotFoundException(Integer accountId) {
        super(MSG.formatted(accountId));
    }
}
