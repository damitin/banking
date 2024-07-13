package com.bankname.banking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class AccountAlreadyClosedException extends RuntimeException {
    private static final String MSG = "Account with ID = %d is already closed";

    public AccountAlreadyClosedException(Integer accountId) {
        super(MSG.formatted(accountId));
    }
}
