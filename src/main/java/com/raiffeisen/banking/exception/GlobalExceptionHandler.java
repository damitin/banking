package com.raiffeisen.banking.exception;

import com.raiffeisen.banking.model.IncorrectDataDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = {
            AccountAlreadyClosedException.class,
            AccountCanNotBeClosedException.class,
            AccountNotCreatedException.class,
            AccountNotFoundException.class,
            DepositOrWithdrawalNotPositiveValueException.class,
            NotEnoughMoneyException.class,
            UserNotFoundException.class
    })
    public ResponseEntity<IncorrectDataDTO> handleException(RuntimeException exception) {
        IncorrectDataDTO data = new IncorrectDataDTO(exception.getMessage());
        return new ResponseEntity<>(data, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<IncorrectDataDTO> handleException(Exception exception) {
        IncorrectDataDTO data = new IncorrectDataDTO(exception.getMessage());
        return new ResponseEntity<>(data, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
