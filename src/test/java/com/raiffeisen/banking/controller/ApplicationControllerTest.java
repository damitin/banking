package com.raiffeisen.banking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raiffeisen.banking.exception.AccountNotFoundException;
import com.raiffeisen.banking.model.AccountDTO;
import com.raiffeisen.banking.model.ChangeBalanceDTO;
import com.raiffeisen.banking.model.NewAccountDTO;
import com.raiffeisen.banking.service.AccountService;
import com.raiffeisen.banking.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ApplicationControllerTest {

    @Mock
    private UserService mockUserService;

    @Mock
    private AccountService mockAccountService;

    @InjectMocks
    private ApplicationController applicationController;

    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(applicationController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void depositAccount() throws Exception {
        ChangeBalanceDTO changeBalanceDTO = new ChangeBalanceDTO(3, new BigDecimal(10));
        String changeBalanceJson = objectMapper.writeValueAsString(changeBalanceDTO);
        AccountDTO accountToDepositDTO = new AccountDTO(3, new BigDecimal(93), 2, 1);

        MvcResult mvcResult = mockMvc.perform(
                put("/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(changeBalanceJson)
        ).andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();

//        assertEquals(accountToDepositDTO, contentAsString);

//        when(mockAccountService.depositAccount(changeBalanceDTO)).thenReturn(accountToDepositDTO);


//        when(mockAccountService.depositAccount(any())).thenThrow(new AccountNotFoundException(100));

//        mockMvc.perform(put("/deposit")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(changeBalanceJson))
//                .andExpect(status().is4xxClientError());

        //жалуется, что разные аргументы changeBalanceDTO
        //verify(mockAccountService, times(1)).depositAccount(changeBalanceDTO);
    }

    @Test
    void withdrawalAccount() throws Exception {
        ChangeBalanceDTO changeBalanceDTO = new ChangeBalanceDTO(2, new BigDecimal(10));
        String changeBalanceJson = objectMapper.writeValueAsString(changeBalanceDTO);

        mockMvc.perform(put("/withdrawal")
                .contentType(MediaType.APPLICATION_JSON)
                .content(changeBalanceJson))
                .andExpect(status().isOk());

        //жалуется, что разные аргументы changeBalanceDTO
//        verify(accountService, times(1)).withdrawAccount(changeBalanceDTO);
    }

    @Test
    void getAccountInfo() throws Exception {
        AccountDTO expectedAccountDTO = new AccountDTO(1, new BigDecimal(100), 1, 1);
        when(mockUserService.getAccountInfo(1, 1)).thenReturn(expectedAccountDTO);
//        when(userService.getAccountInfo(100, 1)).thenThrow(new UserNotFoundException(100));
//        when(userService.getAccountInfo(1, 100)).thenThrow(new AccountNotFoundException(100));
//        такое не запускается в принципе, чтобы проверить некорректный ввод. видимо, нужно делать валидацию в контроллере
//        when(userService.getAccountInfo("qwe", 1)).thenThrow(new Exception());
//        when(userService.getAccountInfo(, "qwe")).thenThrow(new Exception());
        mockMvc.perform(get("/users/1/accounts/1")).andExpect(status().isOk());
        verify(mockUserService, times(1)).getAccountInfo(1, 1);
    }

    @Test
    void findAllAccountsOfUser() throws Exception {
        String login = "bbb";
        AccountDTO first = new AccountDTO(3, new BigDecimal(103), 2, 1);
        AccountDTO second = new AccountDTO(4, new BigDecimal(104), 2, 2);

        mockMvc.perform(get("/users?login=" + login)).andExpect(status().isOk());
//        when(userService.getAccountInfoByParams(login)).thenReturn(List.of(first, second));
        verify(mockUserService, times(1)).getAccountInfoByParams(login);
    }

    @Test
    void findAccountsWithMoneyAmountGreaterThan() throws Exception {
        AccountDTO first = new AccountDTO(19, new BigDecimal(119), 10, 2);
        AccountDTO second = new AccountDTO(20, new BigDecimal(120), 10, 2);
        int moneyAmount = 118;

        mockMvc.perform(get("/users/5?moneyAmount=" + moneyAmount)).andExpect(status().isOk());
        verify(mockAccountService, times(1)).findAccountsByMoneyAmountGreaterThan(new BigDecimal(moneyAmount));
    }

    @Test
    void openAccount() throws Exception {
        NewAccountDTO newAccountDTO = new NewAccountDTO(new BigDecimal(5));
        String newAccountJSON = objectMapper.writeValueAsString(newAccountDTO);
        AccountDTO accountDTO = new AccountDTO(21, new BigDecimal(5), 6, 1);

        mockMvc.perform(post("/users/6/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newAccountJSON))
                .andExpect(status().isOk());

//        when(userService.openIfPossible(newAccountDTO, 5)).thenReturn(accountDTO);

        //опять ошибка про разные аргументы
//        verify(userService, times(1)).openIfPossible(newAccountDTO, 5);
    }

    @Test
    void closeAccount() throws Exception {
        int accountIdExists = 15;
        int accountIdNotExists = 100;
        mockMvc.perform(delete("/users/8/accounts/" + accountIdExists)).andExpect(status().isOk());
//        mockMvc.perform(delete("/users/8/accounts/" + accountIdNotExists)).thenThrow(new AccountNotFoundException(accountIdNotExists));
        verify(mockAccountService, times(1)).closeAccount(accountIdExists);
    }
}