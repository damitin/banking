package com.raiffeisen.banking.model;

import com.raiffeisen.banking.exception.DepositOrWithdrawalNotPositiveValueException;

import java.math.BigDecimal;

public class ChangeBalanceDTO {
    private int accountId;
    private BigDecimal moneyDelta;

    public ChangeBalanceDTO(int accountId, BigDecimal moneyDelta) {
        this.accountId = accountId;
        this.moneyDelta = moneyDelta;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getMoneyDelta() {
        return moneyDelta;
    }

    public void setMoneyDelta(BigDecimal moneyDelta) {
        this.moneyDelta = moneyDelta;
    }

    public void throwIfNegativeValue() {
        if (this.moneyDelta.compareTo(BigDecimal.ZERO) < 0)
            throw new DepositOrWithdrawalNotPositiveValueException(this.getMoneyDelta());
    }
}
