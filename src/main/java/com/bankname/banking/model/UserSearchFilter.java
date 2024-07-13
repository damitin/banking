package com.bankname.banking.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Фильтр по всем параметрам сущности User.
 */
@AllArgsConstructor
@Setter
@Getter
@Builder
public class UserSearchFilter {
    /**
     * ID
     */
    private final Integer id;
    /**
     * Login
     */
    private final String login;
}
