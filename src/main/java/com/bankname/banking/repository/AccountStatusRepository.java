package com.bankname.banking.repository;

import com.bankname.banking.enm.CODE;
import com.bankname.banking.entity.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountStatusRepository extends JpaRepository<AccountStatus, Integer> {
    AccountStatus findByCode(CODE code);
}
