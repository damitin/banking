package com.bankname.banking.utils;

import com.bankname.banking.entity.Account;
import com.bankname.banking.model.AccountDTO;
import com.bankname.banking.model.NewAccountDTO;

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
