CREATE TABLE accounts
(
    id       SERIAL PRIMARY KEY,
    username VARCHAR(50)    NOT NULL,
    balance  NUMERIC(12, 2) NOT NULL
);

CREATE TABLE transactions
(
    id                  SERIAL PRIMARY KEY,
    sender_account_id   INT            NOT NULL REFERENCES accounts (id),
    receiver_account_id INT            NOT NULL REFERENCES accounts (id),
    amount              NUMERIC(12, 2) NOT NULL,
    timestamp           TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);
