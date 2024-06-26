package com.raiffeisen.banking.exception;

public class UserNotFoundException extends RuntimeException {
    private static final String MSG = "There is no User with ID = %s in database";


    public UserNotFoundException(Integer userId) {
        super(MSG.formatted(userId));
    }
}
