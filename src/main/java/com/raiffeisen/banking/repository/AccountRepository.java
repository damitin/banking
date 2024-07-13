package com.raiffeisen.banking.repository;

import com.raiffeisen.banking.enm.CODE;
import com.raiffeisen.banking.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Integer> {

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
            @Param(value = "statusCode") CODE statusCode
            );
}
