package com.bankname.banking.controller;

import com.bankname.banking.model.*;
import com.bankname.banking.service.AccountService;
import com.bankname.banking.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    /**
     * Получить инфомацию по счетам на основе любых параметров счета
     *
     * @param accountSearchFilter
     * @return
     */
    @PostMapping("/accounts")
    public List<AccountDTO> findAccountsByFilter(
            @RequestBody AccountSearchFilter accountSearchFilter
    ) {
        return accountService.findAccountsByFilter(accountSearchFilter);
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

    @PostMapping("/generator")
    public void generator(
            @RequestParam Integer batchCount,
            @RequestParam Integer batchSize
    ) {
        userService.generateUsers(batchCount, batchSize);
        accountService.generateAcocunts(batchCount, batchSize);
    }
}