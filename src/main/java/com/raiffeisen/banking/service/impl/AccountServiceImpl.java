package com.raiffeisen.banking.service.impl;

import com.raiffeisen.banking.entity.Account;
import com.raiffeisen.banking.entity.AccountStatus;
import com.raiffeisen.banking.exception.AccountAlreadyClosedException;
import com.raiffeisen.banking.exception.AccountCanNotBeClosedException;
import com.raiffeisen.banking.exception.AccountNotFoundException;
import com.raiffeisen.banking.exception.NotEnoughMoneyException;
import com.raiffeisen.banking.kafka.KafkaProducer;
import com.raiffeisen.banking.model.AccountDTO;
import com.raiffeisen.banking.model.ChangeBalanceDTO;
import com.raiffeisen.banking.model.NewAccountDTO;
import com.raiffeisen.banking.repository.AccountRepository;
import com.raiffeisen.banking.repository.AccountStatusRepository;
import com.raiffeisen.banking.service.AccountService;
import com.raiffeisen.banking.utils.Mapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final AccountStatusRepository accountStatusRepository;
    private final KafkaProducer kafkaProducer;

    public AccountServiceImpl(AccountRepository accountRepository, AccountStatusRepository accountStatusRepository, KafkaProducer kafkaProducer) {
        this.accountRepository = accountRepository;
        this.accountStatusRepository = accountStatusRepository;
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    @Transactional
    public List<AccountDTO> findAccountsByMoneyAmountGreaterThan(BigDecimal moneyAmount) {
        return accountRepository
                .findAccountsByMoneyAmountGreaterThan(moneyAmount)
                .stream()
                .map(Mapper::toAccountDTO)
                .toList();
    }

    @Override
    @Transactional
    public AccountDTO depositAccount(ChangeBalanceDTO changeBalanceDTO) {
        changeBalanceDTO.throwIfNegativeValue();

        Account accountToDeposit = getAccount(changeBalanceDTO.getAccountId());
        if (accountToDeposit.isClosed()) throw new AccountAlreadyClosedException(changeBalanceDTO.getAccountId());
        accountToDeposit.increaseMoneyAmount(changeBalanceDTO.getMoneyDelta());
        accountToDeposit = accountRepository.saveAndFlush(accountToDeposit);

        AccountDTO accountToDepositDTO = Mapper.toAccountDTO(accountToDeposit);

        kafkaProducer.send("deposit-account-topic", accountToDepositDTO);
        return accountToDepositDTO;
    }

    @Override
    @Transactional
    public AccountDTO withdrawAccount(ChangeBalanceDTO changeBalanceDTO) {
        changeBalanceDTO.throwIfNegativeValue();

        Account accountToWithdraw = getAccount(changeBalanceDTO.getAccountId());
        if (accountToWithdraw.isClosed()) throw new AccountAlreadyClosedException(changeBalanceDTO.getAccountId());
        checkMoneyAmountToWithdraw(changeBalanceDTO, accountToWithdraw);

        accountToWithdraw.decreaseMoneyAmount(changeBalanceDTO.getMoneyDelta());
        accountToWithdraw = accountRepository.saveAndFlush(accountToWithdraw);
        AccountDTO accountToWithdrawDTO = Mapper.toAccountDTO(accountToWithdraw);

        kafkaProducer.send("withdraw-account-topic", accountToWithdrawDTO);
        return accountToWithdrawDTO;
    }

    @Override
    @Transactional
    public AccountDTO openAccount(NewAccountDTO newAccountDTO, Integer userId) {
        Account newAccount = Mapper.toAccount(newAccountDTO, userId);
        return Mapper.toAccountDTO(save(newAccount));
    }

    @Override
    @Transactional
    public AccountDTO closeAccount(Integer accountId) {
        Account accountToClose = getAccount(accountId);

        if (accountToClose.isClosed()) throw new AccountAlreadyClosedException(accountId);
        if (!accountToClose.canBeClosed()) throw new AccountCanNotBeClosedException(accountId);

        AccountStatus status = accountStatusRepository.findByCode(AccountStatus.CODE.CLOSED);
        accountToClose.setStatus(status);
        AccountDTO closedAccountDTO = Mapper.toAccountDTO(accountRepository.save(accountToClose));

        kafkaProducer.send("close-account-topic", closedAccountDTO);
        return closedAccountDTO;
    }

    private void checkMoneyAmountToWithdraw(ChangeBalanceDTO changeBalanceDTO, Account accountToWithdraw) {
        if (changeBalanceDTO.getMoneyDelta().compareTo(accountToWithdraw.getMoneyAmount()) > 0) {
            throw new NotEnoughMoneyException(changeBalanceDTO.getMoneyDelta(), accountToWithdraw.getMoneyAmount());
        }
    }

    private Account getAccount(Integer accountId) {
        return accountRepository
                .findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    private Account save(Account newAccount) {
        newAccount.setStatus(accountStatusRepository.findByCode(AccountStatus.CODE.OPEN));
        return accountRepository.saveAndFlush(newAccount);
    }
}