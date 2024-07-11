package com.raiffeisen.banking.service;

import com.raiffeisen.banking.model.AccountDTO;
import com.raiffeisen.banking.model.NewAccountDTO;
import com.raiffeisen.banking.model.UserSearchFilter;

import java.util.List;

public interface UserService {
    AccountDTO getAccountInfo(Integer userId, Integer accountId);

    List<AccountDTO> getAccountInfoByParams(UserSearchFilter userSearchFilter);

    AccountDTO openIfPossible(NewAccountDTO newAccountDTO, Integer userId);
}
