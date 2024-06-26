drop table if exists banking.user cascade;

create table banking.user
(
    user_id integer generated by default as identity,
    login   varchar(255) unique,
    primary key (user_id)
);