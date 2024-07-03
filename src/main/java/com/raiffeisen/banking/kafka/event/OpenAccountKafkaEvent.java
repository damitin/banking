package com.raiffeisen.banking.kafka.event;

import java.math.BigDecimal;

public class OpenAccountKafkaEvent implements KafkaEvent {
    private int id;
    private BigDecimal moneyAmount;
    private int userId;
    private int accountStatus;

    public OpenAccountKafkaEvent() {
    }

    public OpenAccountKafkaEvent(int id, BigDecimal moneyAmount, int userId, int accountStatus) {
        this.id = id;
        this.moneyAmount = moneyAmount;
        this.userId = userId;
        this.accountStatus = accountStatus;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public BigDecimal getMoneyAmount() {
        return moneyAmount;
    }

    public void setMoneyAmount(BigDecimal moneyAmount) {
        this.moneyAmount = moneyAmount;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(int accountStatus) {
        this.accountStatus = accountStatus;
    }

    @Override
    public String toString() {
        return "OpenAccountEvent{" +
                "id=" + id +
                ", moneyAmount=" + moneyAmount +
                ", userId=" + userId +
                ", accountStatus=" + accountStatus +
                '}';
    }
}
