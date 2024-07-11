package com.raiffeisen.banking.service.impl;

import com.raiffeisen.banking.entity.Account;
import com.raiffeisen.banking.entity.User;
import com.raiffeisen.banking.exception.AccountNotFoundException;
import com.raiffeisen.banking.exception.UserNotFoundException;
import com.raiffeisen.banking.kafka.KafkaProducer;
import com.raiffeisen.banking.model.AccountDTO;
import com.raiffeisen.banking.model.NewAccountDTO;
import com.raiffeisen.banking.model.UserSearchFilter;
import com.raiffeisen.banking.repository.UserRepository;
import com.raiffeisen.banking.service.AccountService;
import com.raiffeisen.banking.service.UserService;
import com.raiffeisen.banking.utils.Mapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Collection;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final AccountService accountService;
    private final KafkaProducer kafkaProducer;

    public UserServiceImpl(UserRepository userRepository, AccountService accountService, KafkaTemplate<String, AccountDTO> kafkaTemplate, KafkaProducer kafkaProducer) {
        this.userRepository = userRepository;
        this.accountService = accountService;
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    public AccountDTO getAccountInfo(Integer userId, Integer accountId) {
        Account currentAccount = findUser(userId)
                .getAccounts()
                .stream()
                .filter(account -> account.getId().equals(accountId))
                .findFirst()
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        return Mapper.toAccountDTO(currentAccount);
    }

    @Override
    public List<AccountDTO> getAccountInfoByParams(UserSearchFilter userSearchFilter) {
        return userRepository
                .findUsersByFilter(userSearchFilter.getId(), userSearchFilter.getLogin())
                .stream()
                .map(User::getAccounts)
                .flatMap(Collection::stream)
                .map(Mapper::toAccountDTO)
                .toList();
    }

    @Override
    public AccountDTO openIfPossible(NewAccountDTO newAccountDTO, Integer userId) {
        findUser(userId);
        AccountDTO openedAccountDTO = accountService.openAccount(newAccountDTO, userId);

        kafkaProducer.send("open-account-topic", openedAccountDTO);

        return openedAccountDTO;
    }

    private User findUser(Integer userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }
}
