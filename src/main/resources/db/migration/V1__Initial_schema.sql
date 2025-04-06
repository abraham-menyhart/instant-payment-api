CREATE TABLE IF NOT EXISTS accounts
(
    id       SERIAL PRIMARY KEY,
    username VARCHAR(50)    NOT NULL,
    balance  NUMERIC(12, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS transactions
(
    id                  SERIAL PRIMARY KEY,
    sender_account_id   INT            NOT NULL REFERENCES accounts (id),
    receiver_account_id INT            NOT NULL REFERENCES accounts (id),
    amount              NUMERIC(12, 2) NOT NULL,
    timestamp           TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO accounts (username, balance)
VALUES ('alice', 1000.00),
       ('bob', 500.00),
       ('charlie', 300.00);