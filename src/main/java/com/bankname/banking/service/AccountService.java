package com.bankname.banking.service;


import com.bankname.banking.model.AccountDTO;
import com.bankname.banking.model.AccountSearchFilter;
import com.bankname.banking.model.ChangeBalanceDTO;
import com.bankname.banking.model.NewAccountDTO;

import java.util.List;

public interface AccountService {
    AccountDTO depositAccount(ChangeBalanceDTO changeBalanceDTO);

    AccountDTO withdrawAccount(ChangeBalanceDTO changeBalanceDTO);

    AccountDTO openAccount(NewAccountDTO newAccountDTO, Integer userId);

    AccountDTO closeAccount(Integer accountId);

    List<AccountDTO> findAccountsByFilter(AccountSearchFilter accountSearchFilter);

    void generateAcocunts(Integer batchCount, Integer batchSize);
}
