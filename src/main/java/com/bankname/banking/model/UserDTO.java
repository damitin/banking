package com.bankname.banking.model;

import com.bankname.banking.entity.Account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@Setter
@Getter
@Builder
public class UserDTO {
    /**
     * ID
     */
    private Integer id;

    /**
     * Login
     */
    private String login;

    /**
     * Accounts
     */
    private Set<Account> accounts;
}
