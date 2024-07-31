DROP TABLE IF EXISTS banking.account CASCADE;

CREATE TABLE banking.account
(
    id           integer generated by default as identity,
    money_amount integer,
    user_id      integer,
    status_id    integer REFERENCES banking.dict_account_status (id),
    primary key (id)
);

CREATE INDEX idx_money_amount ON banking.account (money_amount);
CREATE INDEX idx_hash_status_id ON banking.account USING hash (status_id);