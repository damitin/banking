package com.raiffeisen.banking.service;


import com.raiffeisen.banking.model.*;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
    List<AccountDTO> findAccountsByMoneyAmountGreaterThan(BigDecimal moneyAmount);

    AccountDTO depositAccount(ChangeBalanceDTO changeBalanceDTO);

    AccountDTO withdrawAccount(ChangeBalanceDTO changeBalanceDTO);

    AccountDTO openAccount(NewAccountDTO newAccountDTO, Integer userId);

    AccountDTO closeAccount(Integer accountId);

    List<AccountDTO> findAccountsByFilter(AccountSearchFilter accountSearchFilter);
}
