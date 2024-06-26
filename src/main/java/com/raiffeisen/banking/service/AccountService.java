package com.raiffeisen.banking.service;


import com.raiffeisen.banking.model.AccountDTO;
import com.raiffeisen.banking.model.ChangeBalanceDTO;
import com.raiffeisen.banking.model.NewAccountDTO;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
    List<AccountDTO> findAccountsByMoneyAmountGreaterThan(BigDecimal moneyAmount);

    AccountDTO depositAccount(ChangeBalanceDTO changeBalanceDTO);

    AccountDTO withdrawAccount(ChangeBalanceDTO changeBalanceDTO);

    AccountDTO openAccount(NewAccountDTO newAccountDTO, Integer userId);

    AccountDTO closeAccount(Integer accountId);
}
