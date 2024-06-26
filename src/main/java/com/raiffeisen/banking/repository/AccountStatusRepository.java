package com.raiffeisen.banking.repository;

import com.raiffeisen.banking.entity.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountStatusRepository extends JpaRepository<AccountStatus, Integer> {
    AccountStatus findByCode(AccountStatus.CODE code);
}
