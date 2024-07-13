package com.raiffeisen.banking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raiffeisen.banking.enm.CODE;
import com.raiffeisen.banking.entity.Account;
import com.raiffeisen.banking.entity.AccountStatus;
import com.raiffeisen.banking.exception.*;
import com.raiffeisen.banking.model.*;
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
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    Integer minMoneyAmount = 10;

    BigDecimal positiveMoneyAmount = new BigDecimal(100.00);

    BigDecimal positiveMoneyDelta = new BigDecimal(10.00);
    BigDecimal negativeMoneyDelta = new BigDecimal(-10.00);
    BigDecimal veryBigMoneyDelta = new BigDecimal(1000.00);

    ChangeBalanceDTO positiveExistingAccountChangeBalanceDTO = new ChangeBalanceDTO(existingOpenAccountId, positiveMoneyDelta);
    ChangeBalanceDTO positiveClosedAccountChangeBalanceDTO = new ChangeBalanceDTO(existingClosedAccountId, positiveMoneyDelta);
    ChangeBalanceDTO negativeExistingAccountChangeBalanceDTO = new ChangeBalanceDTO(existingOpenAccountId, negativeMoneyDelta);
    ChangeBalanceDTO positiveNonExistingAccountChangeBalanceDTO = new ChangeBalanceDTO(nonExistingAccountId, positiveMoneyDelta);

    Integer accountStatusOpenId = 1;
    Integer accountStatusClosedId = 1;

    AccountStatus openStatus = new AccountStatus();
    AccountStatus closedStatus = new AccountStatus();

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

    @Nested
    class WithdrawAccountTests {
        @Test
        void withdrawAccount_Success() {
            AccountDTO expectedAccountDTOAfterWithdraw = new AccountDTO(existingOpenAccountId, positiveMoneyAmount.subtract(positiveMoneyDelta), existingUserId, accountStatusOpenId);

            when(mockAccountService.withdrawAccount(positiveExistingAccountChangeBalanceDTO)).thenReturn(expectedAccountDTOAfterWithdraw);

            assertEquals(expectedAccountDTOAfterWithdraw, applicationController.withdrawalAccount(positiveExistingAccountChangeBalanceDTO));
            verify(mockAccountService, times(1)).withdrawAccount(positiveExistingAccountChangeBalanceDTO);
        }

        @Test
        void withdrawAccount_Fail_DepositOrWithdrawalNotPositiveValueException() {

            when(mockAccountService.withdrawAccount(negativeExistingAccountChangeBalanceDTO)).thenThrow(new DepositOrWithdrawalNotPositiveValueException(negativeMoneyDelta));

            assertThrows(DepositOrWithdrawalNotPositiveValueException.class, () -> applicationController.withdrawalAccount(negativeExistingAccountChangeBalanceDTO));
            verify(mockAccountService, times(1)).withdrawAccount(negativeExistingAccountChangeBalanceDTO);
        }

        @Test
        void withdrawAccount_Fail_AccountNotFoundException() {

            when(mockAccountService.withdrawAccount(positiveNonExistingAccountChangeBalanceDTO)).thenThrow(new AccountNotFoundException(nonExistingAccountId));

            assertThrows(AccountNotFoundException.class, () -> applicationController.withdrawalAccount(positiveNonExistingAccountChangeBalanceDTO));
            verify(mockAccountService, times(1)).withdrawAccount(positiveNonExistingAccountChangeBalanceDTO);
        }

        @Test
        void withdrawAccount_Fail_AccountAlreadyClosedException() {

            when(mockAccountService.withdrawAccount(positiveClosedAccountChangeBalanceDTO)).thenThrow(new AccountAlreadyClosedException(existingClosedAccountId));

            assertThrows(AccountAlreadyClosedException.class, () -> applicationController.withdrawalAccount(positiveClosedAccountChangeBalanceDTO));
            verify(mockAccountService, times(1)).withdrawAccount(positiveClosedAccountChangeBalanceDTO);
        }

        @Test
        void withdrawAccount_Fail_NotEnoughMoneyException() {

            when(mockAccountService.withdrawAccount(positiveExistingAccountChangeBalanceDTO)).thenThrow(new NotEnoughMoneyException(veryBigMoneyDelta, positiveMoneyAmount));

            assertThrows(NotEnoughMoneyException.class, () -> applicationController.withdrawalAccount(positiveExistingAccountChangeBalanceDTO));
            verify(mockAccountService, times(1)).withdrawAccount(positiveExistingAccountChangeBalanceDTO);
        }
    }

    @Nested
    class GetAccountInfoTests {

        @Test
        void getAccountInfo_Success() {
            AccountDTO expectedAccountDTO = new AccountDTO(existingOpenAccountId, positiveMoneyAmount, existingUserId, accountStatusOpenId);

            when(mockUserService.getAccountInfo(existingUserId, existingOpenAccountId)).thenReturn(expectedAccountDTO);

            assertEquals(expectedAccountDTO, applicationController.getAccountInfo(existingUserId, existingOpenAccountId));
            verify(mockUserService, times(1)).getAccountInfo(existingUserId, existingOpenAccountId);
        }

        @Test
        void getAccountInfo_UserNotFoundException() {

            when(mockUserService.getAccountInfo(nonExistingUserId, existingOpenAccountId)).thenThrow(new UserNotFoundException(nonExistingUserId));

            assertThrows(UserNotFoundException.class, () -> applicationController.getAccountInfo(nonExistingUserId, existingOpenAccountId));
            verify(mockUserService, times(1)).getAccountInfo(nonExistingUserId, existingOpenAccountId);
        }

        @Test
        void getAccountInfo_AccountNotFoundException() {

            when(mockUserService.getAccountInfo(existingUserId, nonExistingAccountId)).thenThrow(new AccountNotFoundException(nonExistingAccountId));

            assertThrows(AccountNotFoundException.class, () -> applicationController.getAccountInfo(existingUserId, nonExistingAccountId));
            verify(mockUserService, times(1)).getAccountInfo(existingUserId, nonExistingAccountId);
        }
    }

    @Nested
    class FindAllAccountsOfUser {
        String loginExists = "loginExists";
        String loginNotExists = "loginNotExists";
        String loginEmpty = "loginEmpty";
        UserSearchFilter userSearchFilterLoginExists = new UserSearchFilter(null, loginExists);
        UserSearchFilter userSearchFilterLoginNotExists = new UserSearchFilter(null, loginNotExists);
        UserSearchFilter userSearchFilterLoginEmpty = new UserSearchFilter(null, loginEmpty);

        @Test
        void findAllAccountsOfUser_Success_LoginExists() {
            AccountDTO expectedAccountDTO = new AccountDTO(existingOpenAccountId, positiveMoneyAmount, existingUserId, accountStatusOpenId);

            when(mockUserService.getAccountInfoByParams(userSearchFilterLoginExists)).thenReturn(List.of(expectedAccountDTO));

            assertEquals(List.of(expectedAccountDTO), applicationController.findAllAccountsOfUser(userSearchFilterLoginExists));
            verify(mockUserService, times(1)).getAccountInfoByParams(userSearchFilterLoginExists);
        }

        @Test
        void findAllAccountsOfUser_Success_LoginEmpty() {

            when(mockUserService.getAccountInfoByParams(userSearchFilterLoginEmpty)).thenReturn(List.of());

            assertEquals(List.of(), applicationController.findAllAccountsOfUser(userSearchFilterLoginEmpty));
            verify(mockUserService, times(1)).getAccountInfoByParams(userSearchFilterLoginEmpty);
        }

        @Test
        void findAllAccountsOfUser_Success_LoginNotExists() {
            when(mockUserService.getAccountInfoByParams(userSearchFilterLoginNotExists)).thenReturn(List.of());

            assertEquals(List.of(), applicationController.findAllAccountsOfUser(userSearchFilterLoginNotExists));
            verify(mockUserService, times(1)).getAccountInfoByParams(userSearchFilterLoginNotExists);
        }
    }

    @Nested
    class findAllAccountsByParams {

        @Test
        void findAccountsByFilter_Success_AllParameters() {
            AccountSearchFilter accountSearchFilter = AccountSearchFilter.builder()
                    .id(existingOpenAccountId)
                    .moneyAmountMin(minMoneyAmount)
                    .userId(existingUserId)
                    .statusCode(CODE.OPEN)
                    .build();

            AccountDTO expectedAccountDTO = new AccountDTO(existingOpenAccountId, positiveMoneyAmount, existingUserId, accountStatusOpenId);

            when(mockAccountService.findAccountsByFilter(accountSearchFilter)).thenReturn(List.of(expectedAccountDTO));

            assertEquals(List.of(expectedAccountDTO), applicationController.findAccountsByFilter(accountSearchFilter));
            verify(mockAccountService, times(1)).findAccountsByFilter(accountSearchFilter);
        }

        @Test
        void findAccountsByFilter_Success_OneParameter() {
            AccountSearchFilter accountSearchFilter = AccountSearchFilter.builder()
                    .id(nonExistingAccountId)
                    .build();

            when(mockAccountService.findAccountsByFilter(accountSearchFilter)).thenReturn(List.of());

            assertEquals(List.of(), applicationController.findAccountsByFilter(accountSearchFilter));
            verify(mockAccountService, times(1)).findAccountsByFilter(accountSearchFilter);
        }
    }
}