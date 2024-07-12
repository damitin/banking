package com.raiffeisen.banking.model;

import com.raiffeisen.banking.entity.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Фильтр по всем параметрам сущности Account.
 */
@AllArgsConstructor
@Setter
@Getter
@Builder
public class AccountSearchFilter {
    /**
     * ID
     */
    private final Integer id;
    /**
     * Min balance
     */
    private final Integer moneyAmountMin;
    /**
     * Max balance
     */
    private final Integer moneyAmountMax;
    /**
     * Owner's ID
     */
    private final Integer userId;
    /**
     * Active or not
     */
    private final AccountStatus.CODE statusCode;

}
