package com.bankname.banking.repository;

import com.bankname.banking.enm.CODE;
import com.bankname.banking.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    List<Account> findAccountsByFilterOrderById(
            @Param(value = "id") Integer id,
            @Param(value = "moneyAmountMin") Integer moneyAmountMin,
            @Param(value = "moneyAmountMax") Integer moneyAmountMax,
            @Param(value = "userId") Integer userId,
            @Param(value = "statusCode") CODE statusCode
            );
}
