package com.bankname.banking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(schema = "banking", name = "account")
@Builder
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "money_amount")
    private BigDecimal moneyAmount;

    @Column(name = "user_id")
    private Integer userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    private AccountStatus status;

    public Account() {
    }

    public Account(BigDecimal moneyAmount, Integer userId) {
        this.moneyAmount = moneyAmount;
        this.userId = userId;
    }

    public Account(BigDecimal moneyAmount, Integer userId, AccountStatus status) {
        this.moneyAmount = moneyAmount;
        this.userId = userId;
        this.status = status;
    }

    public Account(Integer id, BigDecimal moneyAmount, Integer userId, AccountStatus status) {
        this.id = id;
        this.moneyAmount = moneyAmount;
        this.userId = userId;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public void setUserId(Integer user_id) {
        this.userId = user_id;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus accountStatus) {
        this.status = accountStatus;
    }

    public void increaseMoneyAmount(BigDecimal moneyDelta) {
        this.moneyAmount = this.moneyAmount.add(moneyDelta);
    }

    public void decreaseMoneyAmount(BigDecimal moneyDelta) {
        this.moneyAmount = this.moneyAmount.subtract(moneyDelta);
    }

    public boolean canBeClosed() {
        return this.moneyAmount.compareTo(BigDecimal.ZERO) >= 0;
    }

    public boolean isClosed() {
        return status.isClosed();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(id, account.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
