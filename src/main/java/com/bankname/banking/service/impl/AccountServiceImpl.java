package com.bankname.banking.service.impl;

import com.bankname.banking.enm.CODE;
import com.bankname.banking.entity.Account;
import com.bankname.banking.entity.AccountStatus;
import com.bankname.banking.exception.AccountAlreadyClosedException;
import com.bankname.banking.exception.AccountCanNotBeClosedException;
import com.bankname.banking.exception.AccountNotFoundException;
import com.bankname.banking.exception.NotEnoughMoneyException;
import com.bankname.banking.kafka.KafkaProducer;
import com.bankname.banking.model.AccountDTO;
import com.bankname.banking.model.AccountSearchFilter;
import com.bankname.banking.model.ChangeBalanceDTO;
import com.bankname.banking.model.NewAccountDTO;
import com.bankname.banking.repository.AccountRepository;
import com.bankname.banking.repository.AccountStatusRepository;
import com.bankname.banking.service.AccountService;
import com.bankname.banking.utils.Mapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
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
    public List<AccountDTO> findAccountsByFilter(AccountSearchFilter accountSearchFilter) {
        return accountRepository
                .findAccountsByFilterOrderById(
                        accountSearchFilter.getId(),
                        accountSearchFilter.getMoneyAmountMin(),
                        accountSearchFilter.getMoneyAmountMax(),
                        accountSearchFilter.getUserId(),
                        accountSearchFilter.getStatusCode())
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

        AccountStatus status = accountStatusRepository.findByCode(CODE.CLOSED);
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
        newAccount.setStatus(accountStatusRepository.findByCode(CODE.OPEN));
        return accountRepository.saveAndFlush(newAccount);
    }

    @Override
    public void generateAcocunts(Integer batchCount, Integer batchSize) {
        Random rand = new Random();
        for (int i = 0; i < batchCount; i++) {
            List<Account> accounts = new ArrayList<>();
            for (int j = 0; j < batchSize; j++) {
                Account account = Account.builder()
                        .moneyAmount(new BigDecimal(rand.nextInt(1000) + 10))
                        .userId((i + 1) * (j + 1))
                        .status( (Math.random() <= 0.5) ? accountStatusRepository.findByCode(CODE.OPEN) : accountStatusRepository.findByCode(CODE.CLOSED))
                        .build();
                accounts.add(account);
            }
            var startTime = System.currentTimeMillis();
            accountRepository.saveAll(accounts);
            var endTime = System.currentTimeMillis();
            log.info("Saved batch: batchNum={}, dbSavingTimeMS={}", i + 1, endTime - startTime);
        }
    }
}