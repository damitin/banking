package com.raiffeisen.banking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raiffeisen.banking.entity.Account;
import com.raiffeisen.banking.entity.AccountStatus;
import com.raiffeisen.banking.entity.User;
import com.raiffeisen.banking.exception.*;
import com.raiffeisen.banking.model.ChangeBalanceDTO;
import com.raiffeisen.banking.model.NewAccountDTO;
import com.raiffeisen.banking.repository.AccountRepository;
import com.raiffeisen.banking.repository.AccountStatusRepository;
import com.raiffeisen.banking.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApplicationControllerIntegrationTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AccountStatusRepository accountStatusRepository;

    private ObjectMapper objectMapper;
    @Autowired
    protected MockMvc mockMvc;

    Integer idOfNonExistedUser = 999;
    Integer idOfNonExistedAccount = 999;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @AfterAll
    void tearDown() {
        accountRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Nested
    @Order(1)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class OpenAccountTests {
        @Test
        @Order(1)
        void openAccount_Success() throws Exception {
            NewAccountDTO newAccountDTO = new NewAccountDTO(new BigDecimal(5));
            String newAccountJSON = objectMapper.writeValueAsString(newAccountDTO);
            User user = new User();
            user.setLogin("aaa");
            userRepository.saveAndFlush(user);

            ResultActions resultActions = mockMvc.perform(
                    post("/users/1/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(newAccountJSON)
            );

            resultActions.andExpect(status().isOk());
            String contentAsString = resultActions.andReturn().getResponse().getContentAsString();
            assertEquals("{\"id\":1,\"moneyAmount\":5,\"userId\":1,\"accountStatus\":1}", contentAsString);
        }

        @Test
        @Order(2)
        void openAccount_Fail_UserNotFoundException() throws Exception {
            NewAccountDTO newAccountDTO = new NewAccountDTO(new BigDecimal(5));
            String newAccountJSON = objectMapper.writeValueAsString(newAccountDTO);

            ResultActions resultActions = mockMvc.perform(
                    post("/users/%s/accounts".formatted(idOfNonExistedUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(newAccountJSON)
            );

            resultActions.andExpect(status().isNotFound());
            resultActions.andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException));
            resultActions.andExpect(result -> assertEquals("There is no User with ID = %s in database".formatted(idOfNonExistedUser), result.getResolvedException().getMessage()));
        }

        @Test
        @Order(3)
        void openAccountFailedInternalServerError() throws Exception {
            //TODO придумать как спровоцировать INTERNAL_SERVER_ERROR. Возможно, это не требуется при интеграционном тестировании
        }
    }

    @Nested
    @Order(2)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CloseAccountTests {
        private Integer idOfAccountToClose = 2;

        @Test
        @Order(1)
        void closeAccount_Success() throws Exception {
            Account accountToClose = new Account(new BigDecimal(100), 1, accountStatusRepository.findByCode(AccountStatus.CODE.OPEN));
            accountRepository.saveAndFlush(accountToClose);

            ResultActions resultActions = mockMvc.perform(
                    delete("/users/1/accounts/%s".formatted(idOfAccountToClose)));

            resultActions.andExpect(status().isOk());
            String contentAsString = resultActions.andReturn().getResponse().getContentAsString();
            assertEquals("{\"id\":%s,\"moneyAmount\":100,\"userId\":1,\"accountStatus\":2}".formatted(idOfAccountToClose), contentAsString);
        }

        @Test
        @Order(2)
        void closeAccount_Fail_AccountNotFoundException() throws Exception {
            Integer nonExistedAccountId = 999;

            ResultActions resultActions = mockMvc.perform(
                    delete("/users/1/accounts/%s".formatted(nonExistedAccountId)));

            resultActions.andExpect(status().isNotFound());
            resultActions.andExpect(result -> assertTrue(result.getResolvedException() instanceof AccountNotFoundException));
            resultActions.andExpect(result -> assertEquals("There is no Account with ID = %s in database".formatted(nonExistedAccountId), result.getResolvedException().getMessage()));

        }

        @Test
        @Order(3)
        void closeAccount_Fail_AccountAlreadyClosedException() throws Exception {

            ResultActions resultActions = mockMvc.perform(
                    delete("/users/1/accounts/%s".formatted(idOfAccountToClose)));

            resultActions.andExpect(status().isUnprocessableEntity());
            resultActions.andExpect(result -> assertTrue(result.getResolvedException() instanceof AccountAlreadyClosedException));
            resultActions.andExpect(result -> assertEquals("Account with ID = %s is already closed".formatted(idOfAccountToClose), result.getResolvedException().getMessage()));

        }

        @Test
        @Order(4)
        void closeAccount_Fail_AccountCanNotBeClosed() throws Exception {
            Account accountToClose = accountRepository.findById(2).orElseThrow();
            accountToClose.setStatus(accountStatusRepository.findByCode(AccountStatus.CODE.OPEN));
            accountToClose.setMoneyAmount(new BigDecimal(-100));
            accountRepository.saveAndFlush(accountToClose);

            ResultActions resultActions = mockMvc.perform(
                    delete("/users/1/accounts/%s".formatted(idOfAccountToClose)));

            resultActions.andExpect(status().isUnprocessableEntity());
            resultActions.andExpect(result -> assertTrue(result.getResolvedException() instanceof AccountCanNotBeClosedException));
            resultActions.andExpect(result -> assertEquals("Account with ID = %s cannot be closed because balance is less than 0".formatted(idOfAccountToClose), result.getResolvedException().getMessage()));
        }
    }

    @Nested
    @Order(3)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class DepositAccountTests {
        private Integer idOfAccountToDeposit = 3;

        @Test
        @Order(1)
        void depositAccount_Success() throws Exception {
            Account accountToDeposit = new Account(new BigDecimal(100), 1, accountStatusRepository.findByCode(AccountStatus.CODE.OPEN));
            accountRepository.saveAndFlush(accountToDeposit);

            ChangeBalanceDTO changeBalanceDTO = new ChangeBalanceDTO(idOfAccountToDeposit, new BigDecimal(10));
            String changeBalanceDTOString = objectMapper.writeValueAsString(changeBalanceDTO);

            ResultActions resultActions = mockMvc.perform(
                    put("/deposit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(changeBalanceDTOString)
            );

            resultActions.andExpect(status().isOk());
            String contentAsString = resultActions.andReturn().getResponse().getContentAsString();
            assertEquals("{\"id\":%s,\"moneyAmount\":110,\"userId\":1,\"accountStatus\":1}".formatted(idOfAccountToDeposit), contentAsString);
        }

        @Test
        @Order(2)
        void depositAccount_Fail_DepositOrWithdrawalNotPositiveValueException() throws Exception {
            Account accountToDeposit = new Account(new BigDecimal(100), 1, accountStatusRepository.findByCode(AccountStatus.CODE.OPEN));
            accountRepository.saveAndFlush(accountToDeposit);
            BigDecimal moneyDelta = new BigDecimal(-10);

            ChangeBalanceDTO changeBalanceDTO = new ChangeBalanceDTO(idOfAccountToDeposit, moneyDelta);
            String changeBalanceDTOString = objectMapper.writeValueAsString(changeBalanceDTO);

            ResultActions resultActions = mockMvc.perform(
                    put("/deposit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(changeBalanceDTOString)
            );

            resultActions.andExpect(status().isBadRequest());
            resultActions.andExpect(result -> assertTrue(result.getResolvedException() instanceof DepositOrWithdrawalNotPositiveValueException));
            resultActions.andExpect(result -> assertEquals("Cannot process negative amount %s".formatted(moneyDelta), result.getResolvedException().getMessage()));
        }

        @Test
        @Order(3)
        void depositAccount_Fail_AccountNotFoundException() throws Exception {
            Account accountToDeposit = new Account(new BigDecimal(100), 1, accountStatusRepository.findByCode(AccountStatus.CODE.OPEN));
            accountRepository.saveAndFlush(accountToDeposit);
            BigDecimal moneyDelta = new BigDecimal(10);
            ChangeBalanceDTO changeBalanceDTO = new ChangeBalanceDTO(idOfNonExistedAccount, moneyDelta);
            String changeBalanceDTOString = objectMapper.writeValueAsString(changeBalanceDTO);

            ResultActions resultActions = mockMvc.perform(
                    put("/deposit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(changeBalanceDTOString)
            );

            resultActions.andExpect(status().isNotFound());
            resultActions.andExpect(result -> assertTrue(result.getResolvedException() instanceof AccountNotFoundException));
            resultActions.andExpect(result -> assertEquals("There is no Account with ID = %s in database".formatted(idOfNonExistedAccount), result.getResolvedException().getMessage()));
        }

        @Test
        @Order(4)
        void depositAccount_Fail_AccountAlreadyClosedException() throws Exception {
            Account accountToDeposit = accountRepository.findById(idOfAccountToDeposit).orElseThrow();
            accountToDeposit.setStatus(accountStatusRepository.findByCode(AccountStatus.CODE.CLOSED));
            accountRepository.saveAndFlush(accountToDeposit);
            BigDecimal moneyDelta = new BigDecimal(10);
            ChangeBalanceDTO changeBalanceDTO = new ChangeBalanceDTO(idOfAccountToDeposit, moneyDelta);
            String changeBalanceDTOString = objectMapper.writeValueAsString(changeBalanceDTO);

            ResultActions resultActions = mockMvc.perform(
                    put("/deposit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(changeBalanceDTOString)
            );

            resultActions.andExpect(status().isUnprocessableEntity());
            resultActions.andExpect(result -> assertTrue(result.getResolvedException() instanceof AccountAlreadyClosedException));
            resultActions.andExpect(result -> assertEquals("Account with ID = %d is already closed".formatted(idOfAccountToDeposit), result.getResolvedException().getMessage()));
        }
    }

    @Nested
    @Order(4)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class WithdrawAccountTests {

        private Integer idOfAccountToWithdraw = 4;

        @Test
        @Order(1)
        void withdrawAccount_Success() throws Exception {

            ChangeBalanceDTO changeBalanceDTO = new ChangeBalanceDTO(idOfAccountToWithdraw, new BigDecimal(10));
            String changeBalanceDTOString = objectMapper.writeValueAsString(changeBalanceDTO);

            ResultActions resultActions = mockMvc.perform(
                    put("/withdrawal")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(changeBalanceDTOString)
            );

            resultActions.andExpect(status().isOk());
            String contentAsString = resultActions.andReturn().getResponse().getContentAsString();
            assertEquals("{\"id\":%s,\"moneyAmount\":90,\"userId\":1,\"accountStatus\":1}".formatted(idOfAccountToWithdraw), contentAsString);
        }

        @Test
        @Order(2)
        void withdrawAccount_Fail_DepositOrWithdrawalNotPositiveValueException() throws Exception {
            BigDecimal moneyDelta = new BigDecimal(-10);
            ChangeBalanceDTO changeBalanceDTO = new ChangeBalanceDTO(idOfAccountToWithdraw, moneyDelta);
            String changeBalanceDTOString = objectMapper.writeValueAsString(changeBalanceDTO);

            ResultActions resultActions = mockMvc.perform(
                    put("/withdrawal")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(changeBalanceDTOString)
            );

            resultActions.andExpect(status().isBadRequest());
            resultActions.andExpect(result -> assertTrue(result.getResolvedException() instanceof DepositOrWithdrawalNotPositiveValueException));
            resultActions.andExpect(result -> assertEquals("Cannot process negative amount %s".formatted(moneyDelta), result.getResolvedException().getMessage()));
        }

        @Test
        @Order(3)
        void withdrawAccount_Fail_AccountNotFoundException() throws Exception {
            BigDecimal moneyDelta = new BigDecimal(10);
            ChangeBalanceDTO changeBalanceDTO = new ChangeBalanceDTO(idOfNonExistedAccount, moneyDelta);
            String changeBalanceDTOString = objectMapper.writeValueAsString(changeBalanceDTO);

            ResultActions resultActions = mockMvc.perform(
                    put("/withdrawal")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(changeBalanceDTOString)
            );

            resultActions.andExpect(status().isNotFound());
            resultActions.andExpect(result -> assertTrue(result.getResolvedException() instanceof AccountNotFoundException));
            resultActions.andExpect(result -> assertEquals("There is no Account with ID = %s in database".formatted(idOfNonExistedAccount), result.getResolvedException().getMessage()));
        }

        @Test
        @Order(4)
        void withdrawAccount_Fail_AccountAlreadyClosedException() throws Exception {
            Account accountToDeposit = accountRepository.findById(idOfAccountToWithdraw).orElseThrow();
            accountToDeposit.setStatus(accountStatusRepository.findByCode(AccountStatus.CODE.CLOSED));
            accountRepository.saveAndFlush(accountToDeposit);
            BigDecimal moneyDelta = new BigDecimal(10);
            ChangeBalanceDTO changeBalanceDTO = new ChangeBalanceDTO(idOfAccountToWithdraw, moneyDelta);
            String changeBalanceDTOString = objectMapper.writeValueAsString(changeBalanceDTO);

            ResultActions resultActions = mockMvc.perform(
                    put("/withdrawal")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(changeBalanceDTOString)
            );

            resultActions.andExpect(status().isUnprocessableEntity());
            resultActions.andExpect(result -> assertTrue(result.getResolvedException() instanceof AccountAlreadyClosedException));
            resultActions.andExpect(result -> assertEquals("Account with ID = %d is already closed".formatted(idOfAccountToWithdraw), result.getResolvedException().getMessage()));
        }

        @Test
        @Order(5)
        void withdrawAccount_Fail_NotEnoughMoneyException() throws Exception {
            Account accountToDeposit = accountRepository.findById(idOfAccountToWithdraw).orElseThrow();
            accountToDeposit.setStatus(accountStatusRepository.findByCode(AccountStatus.CODE.OPEN));
            accountRepository.saveAndFlush(accountToDeposit);
            BigDecimal moneyDelta = new BigDecimal(10000);
            ChangeBalanceDTO changeBalanceDTO = new ChangeBalanceDTO(idOfAccountToWithdraw, moneyDelta);
            String changeBalanceDTOString = objectMapper.writeValueAsString(changeBalanceDTO);

            ResultActions resultActions = mockMvc.perform(
                    put("/withdrawal")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(changeBalanceDTOString)
            );

            resultActions.andExpect(status().isUnprocessableEntity());
            resultActions.andExpect(result -> assertTrue(result.getResolvedException() instanceof NotEnoughMoneyException));
            resultActions.andExpect(result -> assertEquals("Cannot withdraw %s. Current money amount %s".formatted(moneyDelta, accountToDeposit.getMoneyAmount()), result.getResolvedException().getMessage()));
        }
    }

    @Nested
    @Order(5)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetAccountInfoTests {

        Integer existingUserId = 1;
        Integer nonExistingUserId = 999;
        Integer existingAccountId = 1;
        Integer nonExistingAccountId = 999;

        @Test
        @Order(1)
        void getAccountInfo_Success() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/users/%s/accounts/%s".formatted(existingUserId, existingAccountId)));

            resultActions.andExpect(status().isOk());
            String contentAsString = resultActions.andReturn().getResponse().getContentAsString();
            assertEquals("{\"id\":%s,\"moneyAmount\":5,\"userId\":%s,\"accountStatus\":1}".formatted(existingUserId, existingAccountId), contentAsString);
        }

        @Test
        @Order(2)
        void getAccountInfo_UserNotFoundException() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/users/%s/accounts/%s".formatted(nonExistingUserId, existingAccountId)));

            resultActions.andExpect(status().isNotFound());
            resultActions.andExpect(result -> assertTrue(result.getResolvedException() instanceof UserNotFoundException));
            resultActions.andExpect(result -> assertEquals("There is no User with ID = %s in database".formatted(nonExistingUserId), result.getResolvedException().getMessage()));
        }

        @Test
        @Order(3)
        void getAccountInfo_AccountNotFoundException() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/users/%s/accounts/%s".formatted(existingUserId, nonExistingAccountId)));

            resultActions.andExpect(status().isNotFound());
            resultActions.andExpect(result -> assertTrue(result.getResolvedException() instanceof AccountNotFoundException));
            resultActions.andExpect(result -> assertEquals("There is no Account with ID = %s in database".formatted(nonExistingAccountId), result.getResolvedException().getMessage()));
        }
    }

    @Nested
    @Order(6)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class FindAllAccountsOfUser {
        String loginExists = "aA";
        String loginNotExists = "aAQWE";

        @Test
        @Order(1)
        void findAllAccountsOfUser_Success_LoginExists() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/users")
                            .param("login", loginExists)
            );

            resultActions.andExpect(status().isOk());
            String contentAsString = resultActions.andReturn().getResponse().getContentAsString();
            String expectedString =
                    "[" +
                    "{\"id\":1,\"moneyAmount\":5,\"userId\":1,\"accountStatus\":1}" +
                    ",{\"id\":2,\"moneyAmount\":-100,\"userId\":1,\"accountStatus\":1}" +
                    ",{\"id\":3,\"moneyAmount\":110,\"userId\":1,\"accountStatus\":2}" +
                    ",{\"id\":4,\"moneyAmount\":90,\"userId\":1,\"accountStatus\":1}" +
                    ",{\"id\":5,\"moneyAmount\":100,\"userId\":1,\"accountStatus\":1}" +
                    "]";
            assertEquals(expectedString, contentAsString);
        }

        @Test
        @Order(2)
        void findAllAccountsOfUser_Success_LoginEmpty() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/users"));

            resultActions.andExpect(status().isOk());
            String contentAsString = resultActions.andReturn().getResponse().getContentAsString();
            String expectedString =
                    "[" +
                            "{\"id\":1,\"moneyAmount\":5,\"userId\":1,\"accountStatus\":1}" +
                            ",{\"id\":2,\"moneyAmount\":-100,\"userId\":1,\"accountStatus\":1}" +
                            ",{\"id\":3,\"moneyAmount\":110,\"userId\":1,\"accountStatus\":2}" +
                            ",{\"id\":4,\"moneyAmount\":90,\"userId\":1,\"accountStatus\":1}" +
                            ",{\"id\":5,\"moneyAmount\":100,\"userId\":1,\"accountStatus\":1}" +
                            "]";
            assertEquals(expectedString, contentAsString);
        }

        @Test
        @Order(3)
        void findAllAccountsOfUser_Success_LoginNotExists() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/users")
                            .param("login", loginNotExists)
            );

            resultActions.andExpect(status().isOk());
            String contentAsString = resultActions.andReturn().getResponse().getContentAsString();
            assertEquals("[]", contentAsString);
        }
    }

    @Nested
    @Order(7)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class FindAccountsWithMoneyAmountGreaterThan {

        Integer moneyAmountHasGreaterValuesInDatabase = 100;
        Integer moneyAmountHasNoGreaterValuesInDatabase = 1000;

        @Test
        @Order(1)
        void findAccountsWithMoneyAmountGreaterThan_Success_AccountsExist() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/users/5")
                            .param("moneyAmount", moneyAmountHasGreaterValuesInDatabase.toString())
            );

            resultActions.andExpect(status().isOk());
            String contentAsString = resultActions.andReturn().getResponse().getContentAsString();
            assertEquals("[{\"id\":3,\"moneyAmount\":110,\"userId\":1,\"accountStatus\":2}]", contentAsString);
        }

        @Test
        @Order(2)
        void findAccountsWithMoneyAmountGreaterThan_Success_AccountsNotExist() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/users/5")
                            .param("moneyAmount", moneyAmountHasNoGreaterValuesInDatabase.toString())
            );

            resultActions.andExpect(status().isOk());
            String contentAsString = resultActions.andReturn().getResponse().getContentAsString();
            assertEquals("[]", contentAsString);
        }
    }
}