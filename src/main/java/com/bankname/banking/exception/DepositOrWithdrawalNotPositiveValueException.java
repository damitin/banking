package com.bankname.banking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.math.BigDecimal;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DepositOrWithdrawalNotPositiveValueException extends RuntimeException {
    private static final String MSG = "Cannot process negative amount %s";

    public DepositOrWithdrawalNotPositiveValueException(BigDecimal moneyDelta) {
        super(MSG.formatted(moneyDelta));
    }
}
