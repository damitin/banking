package com.raiffeisen.banking.controller;

import com.raiffeisen.banking.model.*;
import com.raiffeisen.banking.service.AccountService;
import com.raiffeisen.banking.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "All methods")
@RestController
public class ApplicationController {
    private final UserService userService;
    private final AccountService accountService;

    public ApplicationController(UserService userService, AccountService accountService) {
        this.userService = userService;
        this.accountService = accountService;
    }

    /**
     * Пополнение счета
     *
     * @param changeBalanceDTO
     * @return AccountDTO with new moneyAmount value
     */
    @PutMapping("/deposit")
    public AccountDTO depositAccount(
            @RequestBody ChangeBalanceDTO changeBalanceDTO
    ) {
        return accountService.depositAccount(changeBalanceDTO);
    }

    /**
     * Снятие со счета
     *
     * @param changeBalanceDTO
     * @return AccountDTO with new moneyAmount value
     */
    @PutMapping("/withdrawal")
    public AccountDTO withdrawalAccount(
            @RequestBody ChangeBalanceDTO changeBalanceDTO
    ) {
        return accountService.withdrawAccount(changeBalanceDTO);
    }

    /**
     * Прлучить информацию по счету
     *
     * @param userId
     * @param accountId
     * @return AccountDTO
     */
    @GetMapping("/users/{userId}/accounts/{accountId}")
    @ResponseStatus(HttpStatus.OK)
    public AccountDTO getAccountInfo(
            @PathVariable(name = "userId") Integer userId,
            @PathVariable(name = "accountId") Integer accountId
    ) {
        return userService.getAccountInfo(userId, accountId);
    }

    /**
     * Получить инфомацию по счетам на основе любых параметров пользователя
     *
     * @param userSearchFilter
     * @return List of AccountDTO
     */

    @PostMapping("/users")
    public List<AccountDTO> findAllAccountsOfUser(
            @RequestBody UserSearchFilter userSearchFilter
    ) {
        return userService.getAccountInfoByParams(userSearchFilter);
    }

    @PostMapping("/accounts")
    public List<AccountDTO> findAllAccountsByParams(
            @RequestBody AccountSearchFilter accountSearchFilter
    ) {
        return accountService.findAccountsByFilter(accountSearchFilter);
    }

    // 4.2 Получить инфомацию по счетам на основе каких-то параметров счета
    // TODO Пользователя пока хардкожу в URL из-за конфликта маппингов.
    @GetMapping("/users/5")
    public List<AccountDTO> findAccountsWithMoneyAmountGreaterThan(
            @RequestParam(required = false) BigDecimal moneyAmount
    ) {
        return accountService.findAccountsByMoneyAmountGreaterThan(moneyAmount);
    }

    /**
     * Открыть счет
     *
     * @param newAccountDTO
     * @param userId
     * @return AccountDTO of created account
     */
    @PostMapping("/users/{userId}/accounts")
    public AccountDTO openAccount(
            @RequestBody NewAccountDTO newAccountDTO,
            @PathVariable Integer userId
    ) {
        return userService.openIfPossible(newAccountDTO, userId);
    }


    /**
     * Закрыть счет
     * Переменная userId не нужна, но чтобы не хардкодить ее в URL пока оставляю.
     *
     * @param userId
     * @param accountId
     * @return AccountDTO with new status value
     */
    @DeleteMapping("/users/{userId}/accounts/{accountId}")
    public AccountDTO closeAccount(
            @PathVariable Integer userId,
            @PathVariable Integer accountId
    ) {
        return accountService.closeAccount(accountId);
    }
}
