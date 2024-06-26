package com.raiffeisen.banking.repository;

import com.raiffeisen.banking.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Integer> {
    List<Account> findAccountsByMoneyAmountGreaterThan(BigDecimal moneyAmount);
}
