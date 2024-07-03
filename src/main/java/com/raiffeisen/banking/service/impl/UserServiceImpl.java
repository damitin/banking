package com.raiffeisen.banking.service.impl;

import com.raiffeisen.banking.entity.Account;
import com.raiffeisen.banking.entity.User;
import com.raiffeisen.banking.kafka.event.KafkaEvent;
import com.raiffeisen.banking.kafka.event.OpenAccountKafkaEvent;
import com.raiffeisen.banking.exception.AccountNotFoundException;
import com.raiffeisen.banking.exception.UserNotFoundException;
import com.raiffeisen.banking.kafka.KafkaProducer;
import com.raiffeisen.banking.model.AccountDTO;
import com.raiffeisen.banking.model.NewAccountDTO;
import com.raiffeisen.banking.repository.UserRepository;
import com.raiffeisen.banking.service.AccountService;
import com.raiffeisen.banking.service.UserService;
import com.raiffeisen.banking.utils.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Collection;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final AccountService accountService;
    private final KafkaTemplate<String, KafkaEvent> kafkaTemplate;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final KafkaProducer kafkaProducer;

    public UserServiceImpl(UserRepository userRepository, AccountService accountService, KafkaTemplate<String, KafkaEvent> kafkaTemplate, KafkaProducer kafkaProducer) {
        this.userRepository = userRepository;
        this.accountService = accountService;
        this.kafkaTemplate = kafkaTemplate;
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
    public List<AccountDTO> getAccountInfoByParams(String login) {
        return userRepository
                .findAllByLoginContainsIgnoreCase(login)
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
        OpenAccountKafkaEvent openAccountKafkaEvent = new OpenAccountKafkaEvent(
                openedAccountDTO.getId(),
                openedAccountDTO.getMoneyAmount(),
                openedAccountDTO.getUserId(),
                openedAccountDTO.getAccountStatus()
        );

        kafkaProducer.send("open-account-topic", openAccountKafkaEvent);

        return openedAccountDTO;
    }

    private User findUser(Integer userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }
}
