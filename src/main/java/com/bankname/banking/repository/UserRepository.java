package com.bankname.banking.repository;

import com.bankname.banking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("""
            FROM User u
            WHERE
                (:id IS NULL OR u.id = :id)
                AND (LOWER(u.login) like LOWER(CONCAT('%', :login, '%') ) OR :login IS NULL) 
            """)
    List<User> findUsersByFilter(
            @Param(value = "id") Integer id,
            @Param(value = "login") String login
    );
}

//Пришлось в WHERE поменять параметры login местами, иначе ошибка Caused by: org.postgresql.util.PSQLException: ERROR: function lower(bytea) does not exist
//https://stackoverflow.com/questions/77881433/org-postgresql-util-psqlexception-error-function-lowerbytea-does-not-exist