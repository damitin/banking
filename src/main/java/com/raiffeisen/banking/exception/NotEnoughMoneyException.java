package com.raiffeisen.banking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.math.BigDecimal;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class NotEnoughMoneyException extends RuntimeException {
    private static final String MSG = "Cannot withdraw %s. Current money amount %s";

    public NotEnoughMoneyException(BigDecimal moneyDelta, BigDecimal moneyAmount) {
        super(MSG.formatted(moneyDelta, moneyAmount));
    }
}
