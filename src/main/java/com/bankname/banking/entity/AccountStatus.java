package com.bankname.banking.entity;

import com.bankname.banking.enm.CODE;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.cache.annotation.Cacheable;

import java.util.Objects;

@Entity
@Table(schema = "banking", name = "dict_account_status")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class AccountStatus {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "code")
    @Enumerated(EnumType.STRING)
    private CODE code;

    @Column(name = "description")
    private String description;

    public int getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public CODE getCode() {
        return code;
    }

    public boolean isClosed() {
        return this.code == CODE.CLOSED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountStatus that = (AccountStatus) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
