package com.bankname.banking.service.impl;

import com.bankname.banking.entity.Account;
import com.bankname.banking.entity.User;
import com.bankname.banking.exception.AccountNotFoundException;
import com.bankname.banking.exception.UserNotFoundException;
import com.bankname.banking.kafka.KafkaProducer;
import com.bankname.banking.model.AccountDTO;
import com.bankname.banking.model.NewAccountDTO;
import com.bankname.banking.model.UserSearchFilter;
import com.bankname.banking.repository.UserRepository;
import com.bankname.banking.service.AccountService;
import com.bankname.banking.service.UserService;
import com.bankname.banking.utils.Mapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

@Service
@Slf4j
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

    @Override
    public void generateUsers(Integer batchCount, Integer batchSize) {
        String loginPrefix = "user_";
        for (int i = 0; i < batchCount; i++) {
            List<User> users = new ArrayList<>();
            for (int j = 0; j < batchSize; j++) {
                User user = User.builder()
                        .login(loginPrefix + RandomStringUtils.random(10, true, false))
                        .build();
                users.add(user);
            }
            var startTime = System.currentTimeMillis();
            userRepository.saveAll(users);
            var endTime = System.currentTimeMillis();
            log.info("Saved batch: batchNum={}, dbSavingTimeMS={}", i + 1, endTime - startTime);
        }
    }
}
