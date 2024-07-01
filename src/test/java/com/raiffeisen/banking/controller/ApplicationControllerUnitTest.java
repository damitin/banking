package com.raiffeisen.banking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raiffeisen.banking.entity.Account;
import com.raiffeisen.banking.entity.AccountStatus;
import com.raiffeisen.banking.exception.*;
import com.raiffeisen.banking.model.AccountDTO;
import com.raiffeisen.banking.model.ChangeBalanceDTO;
import com.raiffeisen.banking.model.NewAccountDTO;
import com.raiffeisen.banking.service.AccountService;
import com.raiffeisen.banking.service.UserService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = ApplicationController.class)
class ApplicationControllerUnitTest {

    @MockBean
    private UserService mockUserService;

    @MockBean
    private AccountService mockAccountService;

    @InjectMocks
    @Autowired
    private ApplicationController applicationController;

    private ObjectMapper objectMapper;

    Integer existingUserId = 1;
    Integer nonExistingUserId = 999;

    Integer existingOpenAccountId = 1;
    Integer existingClosedAccountId = 2;
    Integer existingNegativeMoneyAmountAccountId = 3;
    Integer nonExistingAccountId = 999;

    BigDecimal positiveMoneyAmount = new BigDecimal(100.00);
    BigDecimal negativeMoneyAmount = new BigDecimal(-100.00);

    BigDecimal positiveMoneyDelta = new BigDecimal(10.00);
    BigDecimal negativeMoneyDelta = new BigDecimal(-10.00);

    ChangeBalanceDTO positiveExistingAccountChangeBalanceDTO = new ChangeBalanceDTO(existingOpenAccountId, positiveMoneyDelta);
    ChangeBalanceDTO positiveClosedAccountChangeBalanceDTO = new ChangeBalanceDTO(existingClosedAccountId, positiveMoneyDelta);
    ChangeBalanceDTO negativeExistingAccountChangeBalanceDTO = new ChangeBalanceDTO(existingOpenAccountId, negativeMoneyDelta);
    ChangeBalanceDTO positiveNonExistingAccountChangeBalanceDTO = new ChangeBalanceDTO(nonExistingAccountId, positiveMoneyDelta);

    Integer accountStatusOpenId = 1;
    Integer accountStatusClosedId = 1;

    AccountStatus openStatus = new AccountStatus();
    AccountStatus closedStatus = new AccountStatus();


    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        openStatus.setId(accountStatusOpenId);
        closedStatus.setId(accountStatusClosedId);
    }

    @Nested
    class OpenAccountTests {

        @Test
        void openAccount_Success() {
            NewAccountDTO newAccountDTO = new NewAccountDTO(new BigDecimal(5));
            AccountDTO expectedAccountDTO = new AccountDTO(1, new BigDecimal(5), existingUserId, 1);

            when(mockUserService.openIfPossible(newAccountDTO, existingUserId)).thenReturn(expectedAccountDTO);

            assertEquals(expectedAccountDTO, applicationController.openAccount(newAccountDTO, existingUserId));
            verify(mockUserService, times(1)).openIfPossible(newAccountDTO, existingUserId);
        }

        @Test
        void openAccount_Fail_UserNotFoundException() {
            NewAccountDTO newAccountDTO = new NewAccountDTO(new BigDecimal(5));

            when(mockUserService.openIfPossible(newAccountDTO, nonExistingUserId)).thenThrow(new UserNotFoundException(nonExistingUserId));

            assertThrows(UserNotFoundException.class, () -> applicationController.openAccount(newAccountDTO, nonExistingUserId));
            verify(mockUserService, times(1)).openIfPossible(newAccountDTO, nonExistingUserId);
        }
    }

    @Nested

    class CloseAccountTests {
        @Test
        void closeAccount_Success() {
            Account accountToClose = new Account();
            accountToClose.setId(existingOpenAccountId);
            accountToClose.setMoneyAmount(positiveMoneyAmount);
            accountToClose.setUserId(existingUserId);
            accountToClose.setStatus(openStatus);

            AccountDTO expectedClosedAccountDTO = new AccountDTO(existingOpenAccountId, positiveMoneyAmount, existingUserId, accountStatusClosedId);

            when(mockAccountService.closeAccount(existingOpenAccountId)).thenReturn(expectedClosedAccountDTO);

            assertEquals(expectedClosedAccountDTO, applicationController.closeAccount(existingUserId, existingOpenAccountId));
            verify(mockAccountService, times(1)).closeAccount(existingOpenAccountId);
        }

        @Test
        void closeAccount_Fail_AccountNotFoundException() {

            when(mockAccountService.closeAccount(nonExistingAccountId)).thenThrow(new AccountNotFoundException(nonExistingAccountId));

            assertThrows(AccountNotFoundException.class, () -> applicationController.closeAccount(existingUserId, nonExistingAccountId));
            verify(mockAccountService, times(1)).closeAccount(nonExistingAccountId);
        }

        @Test
        void closeAccount_Fail_AccountAlreadyClosedException() {

            when(mockAccountService.closeAccount(existingClosedAccountId)).thenThrow(new AccountAlreadyClosedException(existingClosedAccountId));

            assertThrows(AccountAlreadyClosedException.class, () -> applicationController.closeAccount(existingUserId, existingClosedAccountId));
            verify(mockAccountService, times(1)).closeAccount(existingClosedAccountId);

        }

        @Test
        void closeAccount_Fail_AccountCanNotBeClosed() {

            when(mockAccountService.closeAccount(existingNegativeMoneyAmountAccountId)).thenThrow(new AccountCanNotBeClosedException(existingNegativeMoneyAmountAccountId));

            assertThrows(AccountCanNotBeClosedException.class, () -> applicationController.closeAccount(existingUserId, existingNegativeMoneyAmountAccountId));
            verify(mockAccountService, times(1)).closeAccount(existingNegativeMoneyAmountAccountId);
        }
    }

    @Nested
    class DepositAccountTests {
        @Test
        void depositAccount_Success() {
            AccountDTO expectedAccountDTOAfterDeposit = new AccountDTO(existingOpenAccountId, positiveMoneyAmount.add(positiveMoneyDelta), existingUserId, accountStatusOpenId);

            when(mockAccountService.depositAccount(positiveExistingAccountChangeBalanceDTO)).thenReturn(expectedAccountDTOAfterDeposit);

            assertEquals(expectedAccountDTOAfterDeposit, applicationController.depositAccount(positiveExistingAccountChangeBalanceDTO));
            verify(mockAccountService, times(1)).depositAccount(positiveExistingAccountChangeBalanceDTO);
        }

        @Test
        void depositAccount_Fail_DepositOrWithdrawalNotPositiveValueException() {

            when(mockAccountService.depositAccount(negativeExistingAccountChangeBalanceDTO)).thenThrow(new DepositOrWithdrawalNotPositiveValueException(negativeMoneyDelta));

            assertThrows(DepositOrWithdrawalNotPositiveValueException.class, () -> applicationController.depositAccount(negativeExistingAccountChangeBalanceDTO));
            verify(mockAccountService, times(1)).depositAccount(negativeExistingAccountChangeBalanceDTO);
        }

        @Test
        void depositAccount_Fail_AccountNotFoundException() {

            when(mockAccountService.depositAccount(positiveNonExistingAccountChangeBalanceDTO)).thenThrow(new AccountNotFoundException(nonExistingAccountId));

            assertThrows(AccountNotFoundException.class, () -> applicationController.depositAccount(positiveNonExistingAccountChangeBalanceDTO));
            verify(mockAccountService, times(1)).depositAccount(positiveNonExistingAccountChangeBalanceDTO);
        }

        @Test
        void depositAccount_Fail_AccountAlreadyClosedException() {

            when(mockAccountService.depositAccount(positiveClosedAccountChangeBalanceDTO)).thenThrow(new AccountAlreadyClosedException(existingClosedAccountId));

            assertThrows(AccountAlreadyClosedException.class, () -> applicationController.depositAccount(positiveClosedAccountChangeBalanceDTO));
            verify(mockAccountService, times(1)).depositAccount(positiveClosedAccountChangeBalanceDTO);
        }
    }
}