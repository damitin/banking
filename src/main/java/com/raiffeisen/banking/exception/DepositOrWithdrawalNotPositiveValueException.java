package com.raiffeisen.banking.exception;

import java.math.BigDecimal;

public class DepositOrWithdrawalNotPositiveValueException extends RuntimeException {
    private static final String MSG = "Cannot process negative amount %s";

    public DepositOrWithdrawalNotPositiveValueException(BigDecimal moneyDelta) {
        super(MSG.formatted(moneyDelta));
    }
}
