package com.bankname.banking.entity;

import jakarta.persistence.*;
import lombok.Builder;

import java.util.Objects;
import java.util.Set;

@Entity
@Table(schema = "banking", name = "user")
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer id;

    @Column(name = "login", unique = true)
    private String login;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Set<Account> accounts;

    public User() {
    }

    public User(Integer id, String login, Set<Account> accounts) {
        this.id = id;
        this.login = login;
        this.accounts = accounts;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public Set<Account> getAccounts() {
        return accounts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
