package com.raiffeisen.banking.service.impl;

import com.raiffeisen.banking.entity.Account;
import com.raiffeisen.banking.entity.AccountStatus;
import com.raiffeisen.banking.exception.DepositOrWithdrawalNotPositiveValueException;
import com.raiffeisen.banking.model.AccountDTO;
import com.raiffeisen.banking.model.ChangeBalanceDTO;
import com.raiffeisen.banking.repository.AccountRepository;
import com.raiffeisen.banking.repository.AccountStatusRepository;
import com.raiffeisen.banking.service.AccountService;
import com.raiffeisen.banking.utils.Mapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository mockAccountRepository;

    @Mock
    private AccountStatusRepository mockAccountStatusRepository;

    @InjectMocks
    private AccountServiceImpl accountServiceImpl;


    @Test
    void findAccountsByMoneyAmountGreaterThan() {
        Account first = new Account(new BigDecimal(119), 10, new AccountStatus());
        Account second = new Account(new BigDecimal(120), 10, new AccountStatus());
        BigDecimal moneyAmount = new BigDecimal(118);

//                Mockito
//                .when(accountServiceImpl.findAccountsByMoneyAmountGreaterThan(moneyAmount))
//                .thenReturn(List.of(Mapper.toAccountDTO(first), Mapper.toAccountDTO(second)));

//        verify(mockAccountRepository, times(1))
//                .findAccountsByMoneyAmountGreaterThan(moneyAmount);

    }

//    @Test
//    void depositAccount() {
//        BigDecimal negativeMoneyAmount = new BigDecimal(-1);
//        ChangeBalanceDTO negativeChangeBalanceDTO = new ChangeBalanceDTO(5, negativeMoneyAmount);
//        Mockito
//                .when(accountServiceImpl.depositAccount(negativeChangeBalanceDTO))
//                .thenThrow(new DepositOrWithdrawalNotPositiveValueException(negativeMoneyAmount));
//    }

    @Test
    void withdrawAccount() {
    }

    @Test
    void openAccount() {
    }

    @Test
    void closeAccount() {
    }
}