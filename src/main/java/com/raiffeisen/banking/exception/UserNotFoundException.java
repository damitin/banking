package com.raiffeisen.banking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {
    private static final String MSG = "There is no User with ID = %s in database";


    public UserNotFoundException(Integer userId) {
        super(MSG.formatted(userId));
    }
}
