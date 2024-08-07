drop table if exists banking.user cascade;

create table banking.user
(
    user_id integer generated by default as identity,
    login   varchar(255) unique,
    primary key (user_id)
);

CREATE EXTENSION pg_trgm;
CREATE INDEX idx_gin_trgm_login ON banking.user USING GIN (lower(login) gin_trgm_ops);