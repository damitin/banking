package com.raiffeisen.banking.service.impl;

import com.raiffeisen.banking.entity.Account;
import com.raiffeisen.banking.entity.User;
import com.raiffeisen.banking.event.OpenAccountEvent;
import com.raiffeisen.banking.exception.AccountNotFoundException;
import com.raiffeisen.banking.exception.UserNotFoundException;
import com.raiffeisen.banking.model.AccountDTO;
import com.raiffeisen.banking.model.NewAccountDTO;
import com.raiffeisen.banking.repository.UserRepository;
import com.raiffeisen.banking.service.AccountService;
import com.raiffeisen.banking.service.UserService;
import com.raiffeisen.banking.utils.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final AccountService accountService;
    private final KafkaTemplate<String, OpenAccountEvent> kafkaTemplate;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public UserServiceImpl(UserRepository userRepository, AccountService accountService, KafkaTemplate<String, OpenAccountEvent> kafkaTemplate) {
        this.userRepository = userRepository;
        this.accountService = accountService;
        this.kafkaTemplate = kafkaTemplate;
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
        OpenAccountEvent openAccountEvent = new OpenAccountEvent(
                openedAccountDTO.getId(),
                openedAccountDTO.getMoneyAmount(),
                openedAccountDTO.getUserId(),
                openedAccountDTO.getAccountStatus()
        );
        CompletableFuture<SendResult<String, OpenAccountEvent>> future = kafkaTemplate.send("open-account-topic", STR."\{openedAccountDTO.getId()}", openAccountEvent);
        future.whenComplete((result, exception) -> {
            if (exception != null) {
                LOGGER.error(exception.getMessage(), exception);
            } else {
                LOGGER.info("Message sent to open-account-topic", result.getRecordMetadata());
            }
        });

//        future.join(); //если нужно синхронное взаимодействие с Kafka или избавиться от CompletableFuture как сделано ниже
//
//        SendResult<String, OpenAccountEvent> result = kafkaTemplate.send("open-account-topic", STR."\{openedAccountDTO.getId()}", openAccountEvent).get();
//        LOGGER.info("Return: ", result.getRecordMetadata().topic());
//        LOGGER.info("Return: ", result.getRecordMetadata().partition());
//        LOGGER.info("Return: ", result.getRecordMetadata().offset());


        LOGGER.info("Return: ", openedAccountDTO);
        return openedAccountDTO;
        //TODO убрать логирование и отправку сообщений Kafka в аспекты
    }

    private User findUser(Integer userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }
}
