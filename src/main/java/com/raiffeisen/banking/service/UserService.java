package com.raiffeisen.banking.service;

import com.raiffeisen.banking.model.AccountDTO;
import com.raiffeisen.banking.model.NewAccountDTO;

import java.util.List;

public interface UserService {
    AccountDTO getAccountInfo(Integer userId, Integer accountId);

    List<AccountDTO> getAccountInfoByParams(String login);

    AccountDTO openIfPossible(NewAccountDTO newAccountDTO, Integer userId);
}
