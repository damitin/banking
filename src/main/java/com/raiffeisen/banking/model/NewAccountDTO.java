package com.raiffeisen.banking.model;

import java.math.BigDecimal;

public class NewAccountDTO {
    private BigDecimal moneyAmount;

    public NewAccountDTO() {
    }

    public NewAccountDTO(BigDecimal moneyAmount) {
        this.moneyAmount = moneyAmount;
    }

    public BigDecimal getMoneyAmount() {
        return moneyAmount;
    }

    public void setMoneyAmount(BigDecimal moneyAmount) {
        this.moneyAmount = moneyAmount;
    }

    @Override
    public String toString() {
        return "NewAccountDTO{" +
                "moneyAmount=" + moneyAmount +
                '}';
    }
}
