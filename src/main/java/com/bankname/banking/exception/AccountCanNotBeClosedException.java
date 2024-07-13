package com.bankname.banking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class AccountCanNotBeClosedException extends RuntimeException {
    private static final String MSG = "Account with ID = %d cannot be closed because balance is less than 0";
    public AccountCanNotBeClosedException(Integer accountId) {
        super(MSG.formatted(accountId));
    }
}
