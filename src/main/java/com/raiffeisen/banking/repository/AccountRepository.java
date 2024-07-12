package com.raiffeisen.banking.repository;

import com.raiffeisen.banking.entity.Account;
import com.raiffeisen.banking.entity.AccountStatus;
import com.raiffeisen.banking.model.AccountSearchFilter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Integer> {
    List<Account> findAccountsByMoneyAmountGreaterThan(BigDecimal moneyAmount);

    @Query("""
            FROM Account a
            WHERE
                (:id IS NULL OR a.id = :id)
                AND (:moneyAmountMin IS NULL OR a.moneyAmount >= :moneyAmountMin)
                AND (:moneyAmountMax IS NULL OR a.moneyAmount <= :moneyAmountMax)
                AND (:userId IS NULL OR a.userId = :userId)
                AND (:statusCode IS NULL OR a.status.code = :statusCode)
                
            """)
    List<Account> findAccountsByFilter(
            @Param(value = "id") Integer id,
            @Param(value = "moneyAmountMin") Integer moneyAmountMin,
            @Param(value = "moneyAmountMax") Integer moneyAmountMax,
            @Param(value = "userId") Integer userId,
            @Param(value = "statusCode") AccountStatus.CODE statusCode
            );
}
