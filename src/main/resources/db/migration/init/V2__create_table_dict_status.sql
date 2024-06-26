drop table if exists banking.dict_account_status cascade;

create table banking.dict_account_status
(
    id          integer unique,
    code        varchar(255) unique,
    description varchar(255),
    primary key (id)
);