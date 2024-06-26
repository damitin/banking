package com.raiffeisen.banking.utils;

import com.raiffeisen.banking.entity.Account;
import com.raiffeisen.banking.model.AccountDTO;
import com.raiffeisen.banking.model.NewAccountDTO;

public class Mapper {
    public static AccountDTO toAccountDTO(Account account) {
        return new AccountDTO(
                account.getId(),
                account.getMoneyAmount(),
                account.getUserId(),
                account.getStatus().getId()
        );
    }

    public static Account toAccount(NewAccountDTO newAccountDTO, Integer userId) {
        return new Account(newAccountDTO.getMoneyAmount(), userId);
    }
}
