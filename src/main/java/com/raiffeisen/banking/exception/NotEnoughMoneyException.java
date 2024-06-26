package com.raiffeisen.banking.exception;

import java.math.BigDecimal;

public class NotEnoughMoneyException extends RuntimeException {
    private static final String MSG = "Cannot withdraw %s. Current money amount %s";

    public NotEnoughMoneyException(BigDecimal moneyDelta, BigDecimal moneyAmount) {
        super(MSG.formatted(moneyDelta, moneyAmount));
    }
}
