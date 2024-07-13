package com.bankname.banking.service;

import com.bankname.banking.model.AccountDTO;
import com.bankname.banking.model.NewAccountDTO;
import com.bankname.banking.model.UserSearchFilter;

import java.util.List;

public interface UserService {
    AccountDTO getAccountInfo(Integer userId, Integer accountId);

    List<AccountDTO> getAccountInfoByParams(UserSearchFilter userSearchFilter);

    AccountDTO openIfPossible(NewAccountDTO newAccountDTO, Integer userId);
}
