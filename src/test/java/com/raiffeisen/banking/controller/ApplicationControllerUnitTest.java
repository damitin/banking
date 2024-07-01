package com.raiffeisen.banking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raiffeisen.banking.entity.Account;
import com.raiffeisen.banking.entity.AccountStatus;
import com.raiffeisen.banking.exception.UserNotFoundException;
import com.raiffeisen.banking.model.AccountDTO;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

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

    Integer existingAccountId = 1;
    Integer nonExistingAccountId = 999;

    BigDecimal positiveMoneyAmount = new BigDecimal(100.00);
    BigDecimal negativeMoneyAmount = new BigDecimal(-100.00);

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
        @Order(1)
        void closeAccount_Success() {
            Account accountToClose = new Account();
            accountToClose.setId(existingAccountId);
            accountToClose.setMoneyAmount(positiveMoneyAmount);
            accountToClose.setUserId(existingUserId);
            accountToClose.setStatus(openStatus);

            AccountDTO expectedClosedAccountDTO = new AccountDTO(existingAccountId, positiveMoneyAmount, existingUserId, accountStatusClosedId);

            when(mockAccountService.closeAccount(existingAccountId)).thenReturn(expectedClosedAccountDTO);

            assertEquals(expectedClosedAccountDTO, applicationController.closeAccount(existingUserId, existingAccountId));
            verify(mockAccountService, times(1)).closeAccount(existingAccountId);
        }
    }
}